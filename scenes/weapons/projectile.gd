class_name Projectile
extends Area2D

var speed: float = 300.0
var damage: float = 15.0
var direction: Vector2 = Vector2.ZERO
var penetration_count: int = 1
var life_time: float = 4.0
var explosion_radius: float = 0.0

var weapon_id: String = ""
var weapon_level: int = 1
var is_legendary_fireball: bool = false

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	queue_redraw()
	
	var timer = get_tree().create_timer(life_time)
	timer.timeout.connect(queue_free)

var _has_hit: bool = false

func _physics_process(delta: float) -> void:
	if direction != Vector2.ZERO:
		global_position += direction * speed * delta
		# بک‌آپ هوشمند: تشخیص مطمئن برخورد بر اساس فاصله جهت رفع خطای عدم شلیک روی مرورگر موبایل
		_check_distance_collision()

func _check_distance_collision() -> void:
	if _has_hit:
		return
	var enemies = get_tree().get_nodes_in_group("enemies")
	for enemy in enemies:
		if is_instance_valid(enemy) and enemy.visible and enemy.process_mode != PROCESS_MODE_DISABLED:
			var dist = global_position.distance_to(enemy.global_position)
			# در نظر گرفتن قطر بدنه برخورد زامبی (۱۴ پیکسل شعاع دارد)
			if dist <= 20.0:
				_has_hit = true
				_apply_hit(enemy)
				break

func _on_body_entered(body: Node2D) -> void:
	if _has_hit:
		return
	if body.is_in_group("enemies") and body.has_method("take_damage"):
		_has_hit = true
		_apply_hit(body)

func _apply_hit(enemy_node: Node2D) -> void:
	var player = get_tree().get_first_node_in_group("player")
	var has_dmg_legendary = false
	var has_slow_legendary = false
	
	if player and "upgrade_levels" in player:
		if player.upgrade_levels.get("damage", 0) >= 7:
			has_dmg_legendary = true
		if player.upgrade_levels.get("proj_speed", 0) >= 7:
			has_slow_legendary = true
			
	# اعمال اسلو به دشمن برخورد کرده
	if has_slow_legendary and enemy_node.has_method("apply_slow"):
		enemy_node.apply_slow(0.5, 3.0)
		
	# دمیج افسانه‌ای (انفجار ویرانگر کل گلوله‌ها بر روی هدف)
	if has_dmg_legendary and explosion_radius <= 0:
		explosion_radius = 85.0
		_explode()
		return

	if explosion_radius > 0:
		_explode()
	else:
		if enemy_node.has_method("take_damage"):
			enemy_node.take_damage(damage)
		penetration_count -= 1
		if penetration_count <= 0:
			queue_free()
		else:
			_has_hit = false # اجازه عبور و برخورد با دشمن بعدی

func _explode() -> void:
	var player = get_tree().get_first_node_in_group("player")
	var has_slow_legendary = false
	if player and "upgrade_levels" in player:
		if player.upgrade_levels.get("proj_speed", 0) >= 7:
			has_slow_legendary = true

	for enemy in get_tree().get_nodes_in_group("enemies"):
		if is_instance_valid(enemy) and enemy.visible and enemy.process_mode != PROCESS_MODE_DISABLED:
			if global_position.distance_to(enemy.global_position) <= explosion_radius:
				if enemy.has_method("take_damage"):
					enemy.take_damage(damage)
				if has_slow_legendary and enemy.has_method("apply_slow"):
					enemy.apply_slow(0.5, 3.0)
					
	# قابلیت افسانه‌ای گلوله آتشین (ابرنواختر): اسپاون ۸ مینی گلوله آتشین شعاعی
	if is_legendary_fireball:
		var mini_scene = load("res://scenes/weapons/projectile.tscn")
		if mini_scene:
			for i in range(8):
				var angle = i * (TAU / 8.0)
				var dir = Vector2(cos(angle), sin(angle))
				var mini_proj = mini_scene.instantiate()
				mini_proj.global_position = global_position
				mini_proj.direction = dir
				mini_proj.speed = speed * 0.9
				mini_proj.damage = damage * 0.5
				mini_proj.explosion_radius = 50.0
				mini_proj.is_legendary_fireball = false # ممانعت از بازگشت چرخه‌ای بی‌نهایت
				get_tree().current_scene.add_child(mini_proj)
				
	queue_free()

func _notification(what: int) -> void:
	if what == NOTIFICATION_PREDELETE:
		remove_from_group("projectiles")

func _draw() -> void:
	if explosion_radius > 0:
		draw_circle(Vector2.ZERO, 7.0, Color(1.0, 0.4, 0.1, 0.9))
		draw_circle(Vector2.ZERO, 4.0, Color(1.0, 0.9, 0.3, 1.0))
	else:
		draw_circle(Vector2.ZERO, 6.0, Color(0.2, 0.6, 1.0, 0.9))
		draw_circle(Vector2.ZERO, 3.0, Color.WHITE)
