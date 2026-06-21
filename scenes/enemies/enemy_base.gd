class_name EnemyBase
extends CharacterBody2D

# -------------------------------------------------------------
# EnemyBase - منطق پایه دشمنان بازی
# نقش: تعقیب بازیکن (Player)، مدیریت صدمه دیدن، مردن، و واگذاری امتیاز
# و تراز (XP) به بازیکن از طریق ساختار سیگنال EventBus.
# -------------------------------------------------------------

@export var enemy_name: String = "Zombie"
@export var max_health: float = 30.0
@export var speed: float = 120.0
@export var damage: float = 10.0
@export var xp_value: int = 15
@export var score_value: int = 10

var current_health: float = 30.0
var target_player: CharacterBody2D = null

func _ready() -> void:
	current_health = max_health
	EventBus.connect("player_spawned", Callable(self, "_on_player_spawned"))
	
	# پیدا کردن بازیکن در زمان شروع اگر از قبل اسپون شده باشد
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		target_player = players[0]

func _physics_process(_delta: float) -> void:
	if target_player and is_instance_valid(target_player):
		# حرکت مستقیم به سمت بازیکن (مکانیک اصلی تعقیب در Bullet Heaven)
		var direction = (target_player.global_position - global_position).normalized()
		velocity = direction * speed
		move_and_slide()

func take_damage(amount: float) -> void:
	current_health -= amount
	if current_health <= 0:
		die()

func die() -> void:
	# انتشار رویداد مرگ تا GameManager امتیاز و XP را ثبت کند
	EventBus.emit_signal("enemy_died", self, score_value, xp_value)
	queue_free()

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	target_player = player_node
