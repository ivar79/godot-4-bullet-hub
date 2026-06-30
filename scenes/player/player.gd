class_name Player
extends CharacterBody2D

# -------------------------------------------------------------
# Player - قهرمان و کنترلر اصلی بازی (بهینه‌سازی شده برای موبایل)
# نقش: دریافت ورودی‌های حرکتی (هم دکمه‌های دسکتاپ و هم جوی‌استیک لمسی)،
# مدیریت جان و دفاع، اسپان سلاح‌ها و تجهیزات بقاء.
# -------------------------------------------------------------

@export var default_stats: CharacterData

var stats: CharacterData
var current_health: float = 100.0
var max_health: float = 100.0
var equipped_weapons: Array[WeaponData] = []
var joystick_direction: Vector2 = Vector2.ZERO
var mobile_input_node: MobileInput

# ضریب‌های ارتقاء تراز سراسری برای پایداری و اعمال روی همه سلاح‌های فعلی و جدید
var damage_multiplier: float = 1.0
var fire_rate_multiplier: float = 1.0
var projectile_speed_multiplier: float = 1.0
var magnet_multiplier: float = 1.0

# ذخیره سطح ارتقاهای بازیکن (کلید: نوع ارتقاء، مقدار: سطح از ۱ تا ۷)
# ۱ الی ۵ ستاره استاندارد، ۶ و ۷ ارتقاهای میثیک و لجندری نهایی با ویژگی‌های منحصر به فرد
var upgrade_levels: Dictionary = {
	"speed": 0,
	"damage": 0,
	"fire_rate": 0,
	"armor": 0,
	"magnet": 0,
	"proj_speed": 0
}

# سطوح ارتقاء اسلحه های بازیکن
var weapon_levels: Dictionary = {
	"magic_wand": 1
}

# متغیرهای مربوط به ارتقاء قدرت شخصیت ناشی از مینی‌باس‌ها
var god_state_level: int = 0 # 0: normal, 1: demi-god, 2: god of war
var god_regen_timer: float = 0.0
var god_aura_damage_timer: float = 0.0

# تایمرها برای اعمال خودکار ویژگی‌های افسانه‌ای (Legendary) در سطح ۷
var speed_trail_timer: float = 0.0
var vacuum_timer: float = 0.0

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
		stats = default_stats.duplicate()
	else:
		stats = CharacterData.new()
			
	current_health = stats.max_health
	max_health = stats.max_health
		
	# راه اندازی موبایل اینپوت به صورت کدنویسی به جای اضافه کردن دستی به درخت گره ها
	mobile_input_node = MobileInput.new()
	add_child(mobile_input_node)
	mobile_input_node.joystick_moved.connect(_on_joystick_moved)
	mobile_input_node.joystick_released.connect(_on_joystick_released)
		
	# دادن یک سلاح پیش فرض
	_give_default_weapon()
		
	queue_redraw()

	EventBus.player_spawned.emit(self)
	EventBus.player_health_changed.emit(current_health, max_health)

func _physics_process(delta: float) -> void:
	if !GameManager.is_game_active:
		return
		
	# مدیریت فریم های نامرئی شدن
	if invincible:
		invincible_timer -= delta
		flash_timer += delta
		# فلاش زدن بصری بازیکن در زمان نامرئی بودن
		visible = int(flash_timer * 10) % 2 == 0
		if invincible_timer <= 0:
			invincible = false
			visible = true
			flash_timer = 0.0
		
	# دریافت جهت حرکت بر اساس جوی‌استیک یا کلیدها
	var input_direction = Input.get_vector("move_left", "move_right", "move_up", "move_down")
	if input_direction == Vector2.ZERO:
		input_direction = joystick_direction
		
	velocity = input_direction * stats.base_speed
	move_and_slide()
		
	if input_direction != Vector2.ZERO:
		rotation = input_direction.angle() + PI / 2
		
	# محدود کردن موقعیت بازیکن به نقشه
	global_position.x = clamp(global_position.x, -2960.0, 2960.0)
	global_position.y = clamp(global_position.y, -2960.0, 2960.0)

	# مدیریت ارتقاء‌های بومی ناشی از کشتن مینی‌باس‌ها (Demi-God / God of War)
	if god_state_level >= 1:
		var t = Time.get_ticks_msec() / 1000.0
		if god_state_level == 2:
			modulate = Color(1.0, 0.75 + 0.25 * sin(t * 8.0), 0.2 + 0.3 * cos(t * 6.0))
			
			# خوددرمانی ثانیه‌ای ۳٪ برای خدای جنگ
			god_regen_timer += delta
			if god_regen_timer >= 1.0:
				god_regen_timer = 0.0
				heal(max_health * 0.03)
				
			# هاله آسیب رسان کهکشانی هر ۱ ثانیه
			god_aura_damage_timer += delta
			if god_aura_damage_timer >= 1.0:
				god_aura_damage_timer = 0.0
				_trigger_god_aura_damage()
		else:
			modulate = Color(1.0, 0.85, 0.3) # فیلتر طلایی ثابت برای نیمه‌خدا

	# مدیریت و پردازش ویژگی‌های افسانه‌ای (Legendary) در صورتی که سطح ۷ باشند
	if upgrade_levels.get("speed", 0) >= 7:
		speed_trail_timer += delta
		if speed_trail_timer >= 2.0:
			speed_trail_timer = 0.0
			_trigger_legendary_speed_phantom()
			
	if upgrade_levels.get("magnet", 0) >= 7:
		vacuum_timer += delta
		if vacuum_timer >= 12.0:
			vacuum_timer = 0.0
			_trigger_legendary_vacuum()

func _trigger_legendary_speed_phantom() -> void:
	# ایجاد هاله سرعت فانتوم: دمیج سنگین ۱۲۰ تایی به تمام دشمنان تا فاصله ۱۸۰ پیکسلی بازیکن
	for enemy in get_tree().get_nodes_in_group("enemies"):
		if is_instance_valid(enemy) and enemy.visible and enemy.process_mode != PROCESS_MODE_DISABLED:
			var dist = global_position.distance_to(enemy.global_position)
			if dist <= 180.0:
				if enemy.has_method("take_damage"):
					enemy.take_damage(120.0)
	# افکت بصری فلاش جذاب به رنگ فیروزه‌ای پر سرعت
	var tween = create_tween()
	tween.tween_property(self, "modulate", Color(0.2, 0.9, 0.9), 0.1)
	tween.tween_property(self, "modulate", Color.WHITE, 0.1)

func _trigger_legendary_vacuum() -> void:
	# مگنت سراسری نهایی: کل الماس‌های نقشه مگنت شده و مستقیماً با سرعت بالا به سمت بازیکن حرکت می‌کنند
	for gem in get_tree().get_nodes_in_group("gems"):
		if is_instance_valid(gem):
			gem.is_magnetized = true
			gem.speed = max(gem.speed, 550.0)
	# افکت کهکشانی بنفش چشم‌نواز روی بازیکن
	var tween = create_tween()
	tween.tween_property(self, "modulate", Color(0.8, 0.2, 1.0), 0.15)
	tween.tween_property(self, "modulate", Color.WHITE, 0.15)

func _give_default_weapon() -> void:
	var default_weapon = WeaponData.new()
	default_weapon.weapon_id = "magic_wand"
	default_weapon.weapon_name = "عصای جادویی اولین"
	default_weapon.base_damage = 15.0
	default_weapon.fire_rate = 1.2
	default_weapon.projectile_speed = 350.0
	default_weapon.penetration_count = 2
		
	equip_weapon(default_weapon)

# برای جلوگیری از خطای وابستگی چرخه‌ای (Circular Dependency) در زمان کامپایل از load پویا استفاده می‌کنیم
var WeaponScript = null

func equip_weapon(weapon_data: WeaponData) -> void:
	if weapon_data == null:
		return
		
	# بررسی ظرفیت سلاح (حداکثر ۶ سلاح)
	if equipped_weapons.size() >= 6:
		return
		
	if WeaponScript == null:
		WeaponScript = load("res://scenes/weapons/weapon_base.gd")
		
	if WeaponScript == null:
		printerr("ERROR: Could not load weapon_base.gd!")
		return
		
	var weapon_instance = WeaponScript.new()
	if weapon_instance:
		weapon_instance.weapon_config = weapon_data
		var holder = get_node_or_null("WeaponHolder")
		if holder:
			holder.add_child(weapon_instance)
		else:
			add_child(weapon_instance)
		equipped_weapons.append(weapon_data)

func take_damage(amount: float) -> void:
	if !GameManager.is_game_active:
		return
		
	# اگر در حالت نامرئی باشد، آسیب نمی بیند
	if invincible:
		return
			
	var actual_damage = max(amount - stats.defense, 1.0)
	current_health -= actual_damage
	EventBus.player_health_changed.emit(current_health, max_health)
		
	# فعال سازی افکت و I-Frames
	invincible = true
	invincible_timer = invincible_duration
	flash_timer = 0.0
		
	# افکت رنگی ضربه
	var tween = create_tween()
	tween.tween_property(self, "modulate", Color.GOLDENROD, 0.08)
	tween.tween_property(self, "modulate", Color.WHITE, 0.08)
	
	# قابلیت دفاع افسانه‌ای (سطح ۷) - سپر دفاعی اِجیس: شفا یافتن ۵٪ از حداکثر جان و دمیج خار ۱۵۰ تایی به دشمنان اطراف
	if upgrade_levels.get("armor", 0) >= 7:
		heal(max_health * 0.05)
		for enemy in get_tree().get_nodes_in_group("enemies"):
			if is_instance_valid(enemy) and enemy.visible and enemy.process_mode != PROCESS_MODE_DISABLED:
				var dist = global_position.distance_to(enemy.global_position)
				if dist <= 120.0:
					if enemy.has_method("take_damage"):
						enemy.take_damage(150.0)
		
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
	EventBus.joystick_updated.emit(true, global_position, direction)

func _on_joystick_released() -> void:
	joystick_direction = Vector2.ZERO
	EventBus.joystick_updated.emit(false, Vector2.ZERO, Vector2.ZERO)

func _draw() -> void:
	# اگر حالت خدای جنگ فعال باشد، هاله درخشان کهکشانی زیبایی دور بازیکن رسم می‌کنیم
	if god_state_level >= 2:
		draw_circle(Vector2.ZERO, 150.0, Color(1.0, 0.7, 0.2, 0.12))
		draw_arc(Vector2.ZERO, 150.0, 0, TAU, 32, Color(1.0, 0.8, 0.3, 0.4), 2.0)
		
	# رسم بدنه گرد با رنگ فیروزه ای
	draw_circle(Vector2.ZERO, 14.0, Color(0.2, 0.8, 0.3, 0.9))
	draw_circle(Vector2.ZERO, 7.0, Color.WHITE)

func _trigger_god_aura_damage() -> void:
	# هاله آسیب رسان: 50 دمیج به همه دشمنان در فاصله 150 پیکسلی
	for enemy in get_tree().get_nodes_in_group("enemies"):
		if is_instance_valid(enemy) and enemy.visible and enemy.process_mode != PROCESS_MODE_DISABLED:
			var dist = global_position.distance_to(enemy.global_position)
			if dist <= 150.0:
				if enemy.has_method("take_damage"):
					enemy.take_damage(50.0)

func check_character_boss_milestones(kills: int) -> void:
	if kills == 5 and god_state_level < 1:
		god_state_level = 1
		damage_multiplier += 0.40
		stats.base_speed += 45.0 # افزایش سرعت ۲۵ درصدی (از ۱۸۰ به ۲۲۵)
		max_health += 30.0
		heal(30.0)
		EventBus.show_announcement.emit("⚡ قهرمان به حالت 'نیمه خدایی' ارتقاء یافت! سرعت و قدرت بیشتر! ⚡", Color(0.95, 0.8, 0.2))
		var tween = create_tween()
		tween.tween_property(self, "scale", Vector2(1.2, 1.2), 0.3)
		
	elif kills >= 7 and god_state_level < 2:
		god_state_level = 2
		damage_multiplier += 1.0 # افزایش دمیج سنگین
		EventBus.show_announcement.emit("👑 قهرمان به حالت نهایی 'خدای جنگ' صعود کرد! خوددرمانی و هاله مرگبار فعال شد! 👑", Color(1.0, 0.2, 0.4))
		var tween = create_tween()
		tween.tween_property(self, "scale", Vector2(1.35, 1.35), 0.4)
