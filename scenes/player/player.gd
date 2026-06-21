class_name Player
extends CharacterBody2D

# -------------------------------------------------------------
# Player - منطق و کنترلر بازیکن
# نقش: دریافت ورودی‌های حرکتی موبایل (مانند جوی‌استیک)، حرکت در صفحه،
# مدیریت برخورد با دشمنان و ارتقا دهنده سلاح‌های مجهز شده خودکار.
# -------------------------------------------------------------

@export var default_stats: CharacterData

var stats: CharacterData
var current_health: float = 100.0
var equipped_weapons: Array[WeaponData] = []

func _ready() -> void:
	add_to_group("player")
	if default_stats:
		stats = default_stats
	else:
		# لود پیش‌فرض در صورت نبود ریسورس
		stats = CharacterData.new()
	
	current_health = stats.max_health
	EventBus.emit_signal("player_spawned", self)
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health)

func _physics_process(_delta: float) -> void:
	# دریافت راستای جوی‌استیک یا ورودی لمسی (مناسب موبایل)
	var input_direction = Input.get_vector("move_left", "move_right", "move_up", "move_down")
	velocity = input_direction * stats.base_speed
	move_and_slide()

func take_damage(amount: float) -> void:
	var actual_damage = max(amount - stats.defense, 1.0)
	current_health -= actual_damage
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health)
	
	if current_health <= 0:
		die()

func die() -> void:
	EventBus.emit_signal("player_died")
	queue_free()
