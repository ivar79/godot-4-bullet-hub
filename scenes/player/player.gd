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
var max_health: float = 100.0
var equipped_weapons: Array[WeaponData] = []
var joystick_direction: Vector2 = Vector2.ZERO
var mobile_input_node: MobileInput = null

# Invincibility Frames
var invincible: bool = false
var invincible_timer: float = 0.0
var invincible_duration: float = 0.8
var flash_timer: float = 0.0

# Kill counter for stats
var kill_count: int = 0

func _ready() -> void:
	add_to_group("player")
	
	if default_stats:
		stats = default_stats
	else:
		stats = CharacterData.new()
		
	current_health = stats.max_health
	max_health = stats.max_health
	
	# راه‌اندازی ماژول لمسی تک‌انگشتی موبایل به صورت بومی و اتصال سیگنال‌های آن
	mobile_input_node = MobileInput.new()
	add_child(mobile_input_node)
	mobile_input_node.joystick_moved.connect(_on_joystick_moved)
	mobile_input_node.joystick_released.connect(_on_joystick_released)
	
	# مجهز کردن بازیکن به یک سلاح اولیه
	_give_default_weapon()
	
	queue_redraw()
	EventBus.player_spawned.emit(self)
	EventBus.player_health_changed.emit(current_health, max_health)

func _physics_process(delta: float) -> void:
	if !GameManager.is_game_active:
		return
		
	# مدیریت فریم‌های بی‌مصرفیت
	if invincible:
		invincible_timer -= delta
		flash_timer += delta
		# چشمک زدن بازیکن در زمان بی‌مصرفیت
		visible = int(flash_timer * 10) % 2 == 0
		if invincible_timer <= 0:
			invincible = false
			visible = true
			flash_timer = 0.0
	
	# دریافت همزمان فرمان کیبورد (برای شبیه‌ساز کامپیوتر) و جوی‌استیک موبایل
	var input_direction = Input.get_vector("move_left", "move_right", "move_up", "move_down")
	if input_direction == Vector2.ZERO:
		input_direction = joystick_direction
		
	velocity = input_direction * stats.base_speed
	move_and_slide()
	
	if input_direction != Vector2.ZERO:
		rotation = input_direction.angle() + PI / 2
	
	# محدود کردن موقعیت بازیکن داخل نقشه
	global_position.x = clamp(global_position.x, -2960.0, 2960.0)
	global_position.y = clamp(global_position.y, -2960.0, 2960.0)

func _give_default_weapon() -> void:
	var default_weapon = WeaponData.new()
	default_weapon.weapon_id = "magic_wand"
	default_weapon.weapon_name = "عصای جادویی اولیه"
	default_weapon.base_damage = 15.0
	default_weapon.fire_rate = 1.2
	default_weapon.projectile_speed = 350.0
	default_weapon.penetration_count = 2
	
	equip_weapon(default_weapon)

const WeaponScene: PackedScene = preload("res://scenes/weapons/weapon_base.tscn")

func equip_weapon(weapon_data: WeaponData) -> void:
	if weapon_data == null:
		return
		
	# بررسی محدودیت سلاح (حداکثر ۶ سلاح)
	if equipped_weapons.size() >= 6:
		return
		
	var weapon_instance = WeaponScene.instantiate() as WeaponBase
	weapon_instance.weapon_config = weapon_data
	
	$WeaponHolder.add_child(weapon_instance)
	equipped_weapons.append(weapon_data)

func take_damage(amount: float) -> void:
	if !GameManager.is_game_active:
		return
	
	# در زمان بی‌مصرفیت، آسیبی نمی‌رسد
	if invincible:
		return
		
	var actual_damage = max(amount - stats.defense, 1.0)
	current_health -= actual_damage
	EventBus.player_health_changed.emit(current_health, max_health)
	
	# فعال‌سازی I-Frames
	invincible = true
	invincible_timer = invincible_duration
	flash_timer = 0.0
	
	# افکت لرزش بصری خفیف
	var tween = create_tween()
	tween.tween_property(self, "modulate", Color.GOLDENROD, 0.08)
	tween.tween_property(self, "modulate", Color.WHITE, 0.08)
	
	if current_health <= 0:
		die()

func heal(amount: float) -> void:
	current_health = min(current_health + amount, max_health)
	EventBus.player_health_changed.emit(current_health, max_health)

func die() -> void:
	EventBus.player_died.emit()
	queue_free()

func _on_joystick_moved(direction: Vector2) -> void:
	joystick_direction = direction
	EventBus.joystick_updated.emit(true, mobile_input_node.touch_start_position, direction)

func _on_joystick_released() -> void:
	joystick_direction = Vector2.ZERO
	EventBus.joystick_updated.emit(false, Vector2.ZERO, Vector2.ZERO)

func _draw() -> void:
	# رسم مدل گرافیکی دایره‌ای ساده برای بازیکن
	draw_circle(Vector2.ZERO, 14.0, Color(0.2, 0.8, 0.3, 0.9))
	draw_circle(Vector2.ZERO, 7.0, Color.WHITE)
