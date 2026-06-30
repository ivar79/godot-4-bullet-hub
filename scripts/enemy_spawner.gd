class_name EnemySpawner
extends Node2D

@onready var spawn_timer: Timer = Timer.new()

var player = null
var current_wave: int = 1
var spawn_interval: float = 1.5
var enemies_per_spawn: int = 1
var wave_duration: float = 25.0
var wave_timer: float = 0.0

func _ready() -> void:
	add_child(spawn_timer)
	spawn_timer.wait_time = spawn_interval
	spawn_timer.timeout.connect(_on_spawn_timer_timeout)
	spawn_timer.start()
	
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		player = players[0] as CharacterBody2D
	
	EventBus.player_spawned.connect(_on_player_spawned)
	EventBus.stage_started.connect(_on_stage_started)
	EventBus.final_boss_spawned.connect(_on_final_boss_spawned)
	EventBus.player_level_up.connect(_on_player_level_up)
	EventBus.wave_started.emit(current_wave)

func _on_player_level_up(new_level: int) -> void:
	# اگر در مراحل مضرب ۵ هستیم، به ازای هر ۵ تراز ارتقای کاراکتر، یک مینی‌باس قدرتمند ظاهر می‌شود
	if GameManager.current_stage % 5 == 0 and new_level % 5 == 0:
		EventBus.show_announcement.emit("⚡ رویداد لول " + str(new_level) + ": هجوم مینی‌باس غول‌آسا! ⚡", Color(0.9, 0.2, 0.8))
		_spawn_boss()

func _on_final_boss_spawned() -> void:
	if player == null or !is_instance_valid(player):
		return
	
	var final_boss = EnemyPool.get_enemy()
	if final_boss == null:
		return
	
	final_boss.is_boss = true
	final_boss.is_final_boss = true
	
	# ضریب ارتقاء قدرت بر اساس مراحل
	var stage_mult = 1.0 + (GameManager.current_stage - 1) * 0.4
	var speed_mult = 1.0 + (GameManager.current_stage - 1) * 0.012
	
	final_boss.max_health = 3500.0 * stage_mult
	final_boss.current_health = final_boss.max_health
	final_boss.speed = 105.0 * speed_mult
	final_boss.damage = 60.0 * stage_mult
	final_boss.score_value = 5000
	final_boss.xp_value = 1000
	final_boss.scale = Vector2(4.5, 4.5)
	
	var angle = randf() * TAU
	final_boss.global_position = player.global_position + Vector2(cos(angle), sin(angle)) * 480.0
	
	if final_boss.get_parent():
		final_boss.get_parent().remove_child(final_boss)
	get_parent().add_child(final_boss)

func _on_stage_started(stage_number: int) -> void:
	current_wave = 1
	wave_timer = 0.0
	# با افزایش لول/مرحله، سرعت اسپاون شدن دشمنان اولیه هم بیشتر می‌شود (کمترین فاصله اولیه 0.5 ثانیه)
	spawn_interval = max(0.5, 1.5 - (stage_number - 1) * 0.04)
	spawn_timer.wait_time = spawn_interval
	# با افزایش لول، تعداد اسپاون همزمان اولیه هم بیشتر می‌شود
	enemies_per_spawn = 1 + int((stage_number - 1) / 10)
	spawn_timer.start()
	EventBus.wave_started.emit(current_wave)

func _process(delta: float) -> void:
	if !GameManager.is_game_active or GameManager.is_stage_completed:
		return
	
	wave_timer += delta
	if wave_timer >= wave_duration:
		_advance_wave()

func _advance_wave() -> void:
	wave_timer = 0.0
	current_wave += 1
	
	# فرمول کاهش فاصله اسپاون که به شماره موج و شماره لول بازی بستگی دارد
	var base_interval = 1.5 - (GameManager.current_stage - 1) * 0.04
	spawn_interval = max(0.25, base_interval - (current_wave * 0.15))
	spawn_timer.wait_time = spawn_interval
	
	# تعداد اسپاون همزمان در هر تیک تایمر افزایش می‌یابد
	enemies_per_spawn = 1 + int(current_wave / 3) + int((GameManager.current_stage - 1) / 6)
	
	if current_wave % 5 == 0:
		await get_tree().create_timer(1.0).timeout
		_spawn_boss()
	
	EventBus.wave_completed.emit(current_wave - 1)
	EventBus.wave_started.emit(current_wave)

func _spawn_boss() -> void:
	if player == null or !is_instance_valid(player):
		return
	var boss = EnemyPool.get_enemy()
	if boss == null:
		return
	
	# تنظیم مینی‌باس
	boss.is_boss = true
	
	# ضریب ارتقاء قدرت بر اساس مراحل
	var stage_mult = 1.0 + (GameManager.current_stage - 1) * 0.35
	var speed_mult = 1.0 + (GameManager.current_stage - 1) * 0.012
	
	boss.max_health = (500.0 + current_wave * 60) * stage_mult
	boss.current_health = boss.max_health
	boss.speed = 55.0 * speed_mult
	boss.damage = (40.0 + current_wave * 4) * stage_mult
	boss.score_value = 500
	boss.xp_value = 150
	boss.scale = Vector2(2.5, 2.5)
	boss.enemy_color = Color(0.6, 0.0, 0.8)
	var angle = randf() * TAU
	boss.global_position = player.global_position + Vector2(cos(angle), sin(angle)) * 520.0
	if boss.get_parent():
		boss.get_parent().remove_child(boss)
	get_parent().add_child(boss)

func _on_spawn_timer_timeout() -> void:
	if GameManager.is_game_active and not GameManager.is_stage_completed and player and is_instance_valid(player):
		for i in range(enemies_per_spawn):
			spawn_enemy()

func spawn_enemy() -> void:
	var enemy = EnemyPool.get_enemy()
	if enemy == null:
		return
	
	var spawn_radius = randf_range(500.0, 600.0)
	var angle = randf() * TAU
	var spawn_pos = player.global_position + Vector2(cos(angle), sin(angle)) * spawn_radius
	
	enemy.global_position = spawn_pos
	
	_configure_enemy_for_wave(enemy)
	
	if enemy.get_parent():
		enemy.get_parent().remove_child(enemy)
	get_parent().add_child(enemy)

func _configure_enemy_for_wave(enemy) -> void:
	var roll = randi() % 10
	var type: String
	if current_wave < 3:
		type = "zombie"
	elif roll < 5:
		type = "zombie"
	elif roll < 8:
		type = "runner"
	else:
		type = "tank"
		
	# ضریب ارتقاء قدرت زامبی‌های معمولی بر اساس شماره مرحله به عنوان تعادل سختی
	var stage_mult = 1.0 + (GameManager.current_stage - 1) * 0.35
	var speed_mult = 1.0 + (GameManager.current_stage - 1) * 0.015
	
	match type:
		"zombie":
			enemy.speed = (110.0 + current_wave * 5) * speed_mult
			enemy.max_health = (30.0 + current_wave * 8) * stage_mult
			enemy.damage = (10.0 + current_wave * 2) * stage_mult
			enemy.enemy_color = Color(0.9, 0.2, 0.2)
			enemy.scale = Vector2.ONE
		"runner":
			enemy.speed = (220.0 + current_wave * 8) * speed_mult
			enemy.max_health = (15.0 + current_wave * 4) * stage_mult
			enemy.damage = (6.0 + current_wave * 1.5) * stage_mult
			enemy.enemy_color = Color(1.0, 0.5, 0.1)
			enemy.scale = Vector2(0.75, 0.75)
		"tank":
			enemy.speed = (55.0 + current_wave * 2) * speed_mult
			enemy.max_health = (150.0 + current_wave * 25) * stage_mult
			enemy.damage = (25.0 + current_wave * 5) * stage_mult
			enemy.enemy_color = Color(0.4, 0.1, 0.8)
			enemy.scale = Vector2(1.8, 1.8)
	
	enemy.current_health = enemy.max_health

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	player = player_node
