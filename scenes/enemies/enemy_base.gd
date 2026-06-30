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
var target_player = null
var damage_cooldown: float = 0.8
var damage_timer: float = 0.0
var enemy_color: Color = Color(0.9, 0.2, 0.2)
var is_boss: bool = false
var is_final_boss: bool = false

# مکانیزم کاهش سرعت (اسلو شدن دشمن با سلاح سرعت شلیک افسانه‌ای)
var speed_multiplier: float = 1.0
var slow_timer: float = 0.0

func _ready() -> void:
	add_to_group("enemies")
	reset_state()
	
	EventBus.player_spawned.connect(_on_player_spawned)
	
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		target_player = players[0]

func reset_state() -> void:
	current_health = max_health
	damage_timer = 0.0
	speed_multiplier = 1.0
	slow_timer = 0.0
	is_boss = false
	is_final_boss = false
	scale = Vector2.ONE
	modulate = Color.WHITE
	# رسم یک مربع قرمز ساده به عنوان پلیس‌هولدر بصری
	queue_redraw()

func apply_slow(factor: float, duration: float) -> void:
	speed_multiplier = factor
	slow_timer = duration
	# تغییر رنگ موقت دشمن به آبی مایل به بنفش برای بازخورد بصری اسلو شدن
	modulate = Color(0.4, 0.6, 1.0)

func _physics_process(delta: float) -> void:
	if damage_timer > 0.0:
		damage_timer -= delta

	if slow_timer > 0.0:
		slow_timer -= delta
		if slow_timer <= 0.0:
			speed_multiplier = 1.0
			modulate = Color.WHITE

	if target_player and is_instance_valid(target_player):
		# ۱. الگوی حرکت تعقیبی به سمت بازیکن با در نظر گرفتن کاهش سرعت
		var direction = (target_player.global_position - global_position).normalized()
		velocity = direction * speed * speed_multiplier
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
	EventBus.enemy_died.emit(self, score_value, xp_value)
	
	# بازگشت خاضعانه به استخر حافظه EnemyPool بجای حذف فیزیکی جهت بهینه‌سازی پردازش موبایل
	EnemyPool.return_to_pool(self)

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	target_player = player_node

func _draw() -> void:
	if is_final_boss:
		# رسم باس نهایی: پادشاه تاریکی با بدنه تیره، دورگیری قرمز درخشان و دو شاخ دفاعی تیز
		draw_rect(Rect2(-24, -24, 48, 48), Color(0.12, 0.05, 0.25), true)
		draw_rect(Rect2(-24, -24, 48, 48), Color(0.9, 0.1, 0.1), false, 4.0)
		draw_line(Vector2(-24, -24), Vector2(-36, -36), Color(0.9, 0.1, 0.1), 3.0)
		draw_line(Vector2(24, -24), Vector2(36, -36), Color(0.9, 0.1, 0.1), 3.0)
	elif is_boss:
		# رسم مینی‌باس: بدنه بنفش پررنگ با حاشیه طلایی رنگ برای ابهت بیشتر
		draw_rect(Rect2(-16, -16, 32, 32), Color(0.45, 0.05, 0.65), true)
		draw_rect(Rect2(-16, -16, 32, 32), Color(1.0, 0.85, 0.2), false, 3.0)
	else:
		draw_rect(Rect2(-14, -14, 28, 28), enemy_color, true)
		draw_rect(Rect2(-14, -14, 28, 28), Color.BLACK, false, 2.0)
