class_name Player
extends CharacterBody2D

# -------------------------------------------------------------
# Player - منطق و کنترلر بازیکن (بهینه‌سازی شده برای موبایل)
# نقش: دریافت ورودی‌های حرکتی موبایل (هم کیبورد و هم جوی‌استیک لمسی تک انگشتی)،
# مدیریت جان و صدمه، و اسپون خودکار سلاح‌ها و تجهیزات بقاء در هولدر تفنگ.
# -------------------------------------------------------------

@export var default_stats: CharacterData

var stats: CharacterData
var current_health: float = 100.0
var equipped_weapons: Array[WeaponData] = []
var joystick_direction: Vector2 = Vector2.ZERO
var mobile_input_node: MobileInput = null

func _ready() -> void:
	add_to_group("player")
	
	if default_stats:
		stats = default_stats
	else:
		stats = CharacterData.new()
		
	current_health = stats.max_health
	
	# راه‌اندازی ماژول لمسی تک‌انگشتی موبایل به صورت بومی و اتصال سیگنال‌های آن
	mobile_input_node = MobileInput.new()
	add_child(mobile_input_node)
	mobile_input_node.connect("joystick_moved", Callable(self, "_on_joystick_moved"))
	mobile_input_node.connect("joystick_released", Callable(self, "_on_joystick_released"))
	
	# مجهز کردن بازیکن به یک سلاح اولیه (مثلاً Magic Wand) تا بلافاصله شلیک آغاز شود
	_give_default_weapon()
	
	EventBus.emit_signal("player_spawned", self)
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health)

func _physics_process(_delta: float) -> void:
	if !GameManager.is_game_active:
		return
		
	# دریافت همزمان فرمان کیبورد (برای شبیه‌ساز کامپیوتر) و جوی‌استیک موبایل
	var input_direction = Input.get_vector("move_left", "move_right", "move_up", "move_down")
	if input_direction == Vector2.ZERO:
		input_direction = joystick_direction
		
	velocity = input_direction * stats.base_speed
	move_and_slide()

func _give_default_weapon() -> void:
	var default_weapon = WeaponData.new()
	default_weapon.weapon_id = "magic_wand"
	default_weapon.weapon_name = " عصای جادویی اولیه"
	default_weapon.base_damage = 15.0
	default_weapon.fire_rate = 1.2
	default_weapon.projectile_speed = 350.0
	default_weapon.penetration_count = 2
	
	equip_weapon(default_weapon)

func equip_weapon(weapon_data: WeaponData) -> void:
	if weapon_data == null:
		return
		
	# لود کردن گنجینه سلاح خودکار
	var weapon_scene = preload("res://scenes/weapons/weapon_base.tscn")
	var weapon_instance = weapon_scene.instantiate() as WeaponBase
	weapon_instance.weapon_config = weapon_data
	
	$WeaponHolder.add_child(weapon_instance)
	equipped_weapons.append(weapon_data)

func take_damage(amount: float) -> void:
	if !GameManager.is_game_active:
		return
		
	var actual_damage = max(amount - stats.defense, 1.0)
	current_health -= actual_damage
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health)
	
	# افکت لرزش بصری خفیف و قرمزی زمان صدمه
	var tween = create_tween()
	tween.tween_property(self, "modulate", Color.GOLDENROD, 0.08)
	tween.tween_property(self, "modulate", Color.WHITE, 0.08)
	
	if current_health <= 0:
		die()

func heal(amount: float) -> void:
	current_health = min(current_health + amount, stats.max_health)
	EventBus.emit_signal("player_health_changed", current_health, stats.max_health)

func die() -> void:
	EventBus.emit_signal("player_died")
	queue_free()

func _on_joystick_moved(direction: Vector2) -> void:
	joystick_direction = direction
	EventBus.emit_signal("joystick_updated", true, mobile_input_node.touch_start_position, direction)

func _on_joystick_released() -> void:
	joystick_direction = Vector2.ZERO
	EventBus.emit_signal("joystick_updated", false, Vector2.ZERO, Vector2.ZERO)

func _draw() -> void:
	# رسم مدل گرافیکی دایره‌ای ساده برای بازیکن
	draw_circle(Vector2.ZERO, 16.0, Color(0.2, 0.8, 0.3, 0.9)) # دایره سبز رنگ
	draw_circle(Vector2.ZERO, 8.0, Color.WHITE)  # دایره درونی نماد هسته حیات
