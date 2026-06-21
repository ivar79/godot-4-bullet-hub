class_name EnemyBase
extends CharacterBody2D

# -------------------------------------------------------------
# EnemyBase - منطق پایه دشمنان بازی (خوش‌دست شده برای موبایل)
# نقش: تعقیب بازیکن (Player)، اعمال صدمه تماسی ضربانی به بازیکن،
# و به محض اتمام خون، بازگشت به استخر EnemyPool بجای نابودی کامل.
# -------------------------------------------------------------

@export var enemy_name: String = "Zombie"
@export var max_health: float = 30.0
@export var speed: float = 120.0
@export var damage: float = 10.0
@export var xp_value: int = 15
@export var score_value: int = 10

var current_health: float = 30.0
var target_player: Player = null
var damage_cooldown: float = 0.8
var damage_timer: float = 0.0

func _ready() -> void:
	add_to_group("enemies")
	reset_state()
	
	EventBus.connect("player_spawned", Callable(self, "_on_player_spawned"))
	
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		target_player = players[0]

func reset_state() -> void:
	current_health = max_health
	damage_timer = 0.0
	# رسم یک مربع قرمز ساده به عنوان پلیس‌هولدر بصری
	queue_redraw()

func _physics_process(delta: float) -> void:
	if damage_timer > 0.0:
		damage_timer -= delta

	if target_player and is_instance_valid(target_player):
		# ۱. الگوی حرکت تعقیبی به سمت بازیکن
		var direction = (target_player.global_position - global_position).normalized()
		velocity = direction * speed
		move_and_slide()
		
		# ۲. تشخیص تماس بدنی و تحمیل آسیب ضربانی به مبارز در صورت نزدیکی زیاد
		var dist = global_position.distance_to(target_player.global_position)
		if dist <= 32.0 and damage_timer <= 0.0:
			target_player.take_damage(damage)
			damage_timer = damage_cooldown

func take_damage(amount: float) -> void:
	current_health -= amount
	# افکت لرزش بصری خفیف
	var tween = create_tween()
	tween.tween_property(self, "modulate", Color.RED, 0.1)
	tween.tween_property(self, "modulate", Color.WHITE, 0.1)
	
	if current_health <= 0:
		die()

func die() -> void:
	# انتشار رویداد جهت ثبت امتیاز و انداختن جم تجربه (XP Gem) روی زمین
	EventBus.emit_signal("enemy_died", self, score_value, xp_value)
	
	# بازگشت خاضعانه به استخر حافظه EnemyPool بجای حذف فیزیکی جهت بهینه‌سازی پردازش موبایل
	EnemyPool.return_to_pool(self)

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	target_player = player_node as Player

func _draw() -> void:
	# ترسیم یک مربع یا رنگ قرمز ساده به عنوان نماد زامبی فاقد اسپرایت
	draw_rect(Rect2(-14, -14, 28, 28), Color(0.9, 0.2, 0.2, 0.8), true)
	# رسم حاشیه مشکی کوچک
	draw_rect(Rect2(-14, -14, 28, 28), Color.BLACK, false, 2.0)
