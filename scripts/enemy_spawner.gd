class_name EnemySpawner
extends Node2D

# -------------------------------------------------------------
# EnemySpawner - اسپاونر دوره‌ای و موج‌محور دشمنان
# نقش: تکثیر اتوماتیک دشمنان با فواصل قابل تنظیم پیرامون بازیکن،
# افزایش سختی با ارتقای شماره موج بقا (Wave) و تزریق کالیبراسیون دشواری.
# -------------------------------------------------------------

@onready var spawn_timer: Timer = Timer.new()

var player: Player = null
var current_wave: int = 1
var spawn_interval: float = 1.5
var enemies_per_spawn: int = 1
var wave_duration: float = 25.0 # مدت زمان هر موج بقاء
var wave_timer: float = 0.0

func _ready() -> void:
	add_child(spawn_timer)
	spawn_timer.wait_time = spawn_interval
	spawn_timer.connect("timeout", Callable(self, "_on_spawn_timer_timeout"))
	spawn_timer.start()
	
	# پیدا کردن بازیکن اسپون شده در صحنه
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		player = players[0] as Player
		
	EventBus.connect("player_spawned", Callable(self, "_on_player_spawned"))
	EventBus.emit_signal("wave_started", current_wave)

func _process(delta: float) -> void:
	if !GameManager.is_game_active:
		return
		
	# شمارش معکوس زمان موج کنونی جهت تغییر دشواری
	wave_timer += delta
	if wave_timer >= wave_duration:
		_advance_wave()

func _advance_wave() -> void:
	wave_timer = 0.0
	current_wave += 1
	
	# افزایش پیچیدگی بقاء با ارتقای تعداد و سرعت اسپاون دشمنان
	spawn_interval = max(0.4, 1.5 - (current_wave * 0.15))
	spawn_timer.wait_time = spawn_interval
	enemies_per_spawn = 1 + int(current_wave / 3)
	
	EventBus.emit_signal("wave_completed", current_wave - 1)
	EventBus.emit_signal("wave_started", current_wave)

func _on_spawn_timer_timeout() -> void:
	if GameManager.is_game_active and player and is_instance_valid(player):
		for i in range(enemies_per_spawn):
			spawn_enemy()

func spawn_enemy() -> void:
	# دریافت نماد زامبی از استخر بهینه‌ساز EnemyPool بجای لود از دیسک
	var enemy = EnemyPool.get_enemy()
	if enemy == null:
		return
		
	# محاسبه زاویه تصادفی خارج از کادر دوربین جهت احضار دشمن (شعاع ۵۰۰ تا ۶۰۰ پیکسل)
	var spawn_radius = randf_range(500.0, 600.0)
	var angle = randf() * TAU
	var spawn_pos = player.global_position + Vector2(cos(angle), sin(angle)) * spawn_radius
	
	enemy.global_position = spawn_pos
	
	# ارتقای مقیاس جان و دمیج دشمن متناسب با شماره موج زنده ماندن
	enemy.max_health = 30.0 + (current_wave - 1) * 8.0
	enemy.damage = 10.0 + (current_wave - 1) * 3.0
	enemy.speed = 120.0 + min(80.0, (current_wave - 1) * 10.0)
	enemy.current_health = enemy.max_health
	
	# افزودن به درخت صحنه روت گیم‌پلی
	if enemy.get_parent():
		enemy.get_parent().remove_child(enemy)
	get_parent().add_child(enemy)

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	player = player_node as Player
