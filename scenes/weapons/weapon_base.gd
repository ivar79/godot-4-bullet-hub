class_name WeaponBase
extends Node2D

@export var weapon_config: WeaponData

var fire_timer: Timer

var target_enemy: CharacterBody2D

const MAX_PROJECTILES = 40

func _ready() -> void:
	if fire_timer == null:
		# ایجاد داینامیک تایمر جهت استقلال اسکریپت از سین و جلوگیری از کرش لود در موبایل
		fire_timer = Timer.new()
		add_child(fire_timer)
		fire_timer.autostart = true
		fire_timer.timeout.connect(_on_fire_timer_timeout)
		
	if weapon_config:
		setup_weapon()

func get_actual_damage() -> float:
	var base = weapon_config.base_damage if weapon_config else 10.0
	var player = get_tree().get_first_node_in_group("player")
	if player:
		var w_lvl = player.weapon_levels.get(weapon_config.weapon_id, 1) if weapon_config else 1
		# افزایش قدرت به ازای سطح اسلحه
		if weapon_config:
			match weapon_config.weapon_id:
				"magic_wand":
					base = base * (1.0 + (w_lvl - 1) * 0.20)
				"fireball":
					base = base * (1.0 + (w_lvl - 1) * 0.25)
				"lightning_chain":
					base = base * (1.0 + (w_lvl - 1) * 0.20)
		if "damage_multiplier" in player:
			return base * player.damage_multiplier
	return base

func get_actual_projectile_speed() -> float:
	var base = weapon_config.projectile_speed if weapon_config else 300.0
	var player = get_tree().get_first_node_in_group("player")
	if player:
		var w_lvl = player.weapon_levels.get(weapon_config.weapon_id, 1) if weapon_config else 1
		if weapon_config and weapon_config.weapon_id == "magic_wand":
			base = base * (1.0 + (w_lvl - 1) * 0.15)
		if "projectile_speed_multiplier" in player:
			return base * player.projectile_speed_multiplier
	return base

func get_actual_fire_rate() -> float:
	var base = weapon_config.fire_rate if weapon_config else 1.0
	var player = get_tree().get_first_node_in_group("player")
	if player:
		var w_lvl = player.weapon_levels.get(weapon_config.weapon_id, 1) if weapon_config else 1
		if weapon_config and weapon_config.weapon_id == "magic_wand" and w_lvl >= 7:
			base = base * 2.0
		if "fire_rate_multiplier" in player:
			return base * player.fire_rate_multiplier
	return base

func setup_weapon() -> void:
	fire_timer.wait_time = max(0.05, 1.0 / get_actual_fire_rate())
	fire_timer.start()

func update_fire_rate() -> void:
	if fire_timer:
		fire_timer.wait_time = max(0.05, 1.0 / get_actual_fire_rate())

func _on_fire_timer_timeout() -> void:
	if weapon_config and weapon_config.weapon_id == "lightning_chain":
		_chain_lightning()
		return
	
	target_enemy = get_nearest_enemy()
	if target_enemy:
		shoot_at_target(target_enemy)

func get_nearest_enemy() -> CharacterBody2D:
	var enemies = get_tree().get_nodes_in_group("enemies")
	var nearest: CharacterBody2D
	var min_dist: float = INF
	
	for enemy in enemies:
		if is_instance_valid(enemy) and enemy.visible:
			var dist = global_position.distance_to(enemy.global_position)
			if dist < min_dist:
				min_dist = dist
				nearest = enemy
	return nearest

func shoot_at_target(enemy: CharacterBody2D) -> void:
	if !is_instance_valid(enemy):
		return
	
	var base_dir = (enemy.global_position - global_position).normalized()
	_spawn_projectile_instance(enemy, base_dir)
	
	var player = get_tree().get_first_node_in_group("player")
	if player:
		var w_lvl = player.weapon_levels.get(weapon_config.weapon_id if weapon_config else "", 1)
		# عصای جادویی سطح ۶: شلیک ۱ گلوله کمکی همزمان با انحراف ۲۰ درجه
		# عصای جادویی سطح ۷: شلیک ۴ گلوله اضافی همزمان با انحراف‌های گوناگون (طوفان جادویی)
		if weapon_config and weapon_config.weapon_id == "magic_wand":
			if w_lvl == 6:
				_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(20.0)))
			elif w_lvl >= 7:
				_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(20.0)))
				_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(-20.0)))
				_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(35.0)))
				_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(-35.0)))
				
		# قابلیت افسانه‌ای سرعت شلیک (سطح ۷): شلیک ۲ گلوله کمکی با انحراف ۱۸ درجه
		if "upgrade_levels" in player and player.upgrade_levels.get("fire_rate", 0) >= 7:
			_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(18.0)))
			_spawn_projectile_instance(enemy, base_dir.rotated(deg_to_rad(-18.0)))

func _spawn_projectile_instance(_enemy: CharacterBody2D, dir: Vector2) -> void:
	if get_tree().get_nodes_in_group("projectiles").size() >= MAX_PROJECTILES:
		return
		
	var proj_scene = load("res://scenes/weapons/projectile.tscn")
	if proj_scene == null:
		return
	var proj_instance = proj_scene.instantiate()
	
	proj_instance.global_position = global_position
	proj_instance.direction = dir
	
	var player = get_tree().get_first_node_in_group("player")
	var w_lvl = 1
	if player:
		w_lvl = player.weapon_levels.get(weapon_config.weapon_id if weapon_config else "", 1)
		
	if weapon_config:
		proj_instance.weapon_id = weapon_config.weapon_id
		proj_instance.weapon_level = w_lvl
		proj_instance.damage = get_actual_damage()
		proj_instance.speed = get_actual_projectile_speed()
		proj_instance.penetration_count = weapon_config.penetration_count
		
		if weapon_config.weapon_id == "magic_wand":
			if w_lvl >= 7:
				proj_instance.scale = Vector2(2.2, 2.2) # بسیار عظیم‌الجثه
				proj_instance.penetration_count = 9999 # نفوذ بی‌نهایت!
		elif weapon_config.weapon_id == "fireball":
			proj_instance.explosion_radius = 80.0 + (w_lvl - 1) * 15.0
			if w_lvl >= 7:
				proj_instance.is_legendary_fireball = true
				proj_instance.scale = Vector2(1.5, 1.5)
	
	proj_instance.add_to_group("projectiles")
	get_tree().current_scene.add_child(proj_instance)

func _chain_lightning() -> void:
	var enemies = get_tree().get_nodes_in_group("enemies")
	var valid_enemies = []
	for enemy in enemies:
		if is_instance_valid(enemy) and enemy.visible:
			valid_enemies.append(enemy)
			
	# مرتب‌سازی دستی بر اساس فاصله برای حداکثر سازگاری و جلوگیری از مشکلات پارسر
	for i in range(valid_enemies.size()):
		var min_idx = i
		var min_dist = global_position.distance_to(valid_enemies[i].global_position)
		for j in range(i + 1, valid_enemies.size()):
			var dist = global_position.distance_to(valid_enemies[j].global_position)
			if dist < min_dist:
				min_dist = dist
				min_idx = j
		if min_idx != i:
			var temp = valid_enemies[i]
			valid_enemies[i] = valid_enemies[min_idx]
			valid_enemies[min_idx] = temp

	var player = get_tree().get_first_node_in_group("player")
	var w_lvl = 1
	if player:
		w_lvl = player.weapon_levels.get("lightning_chain", 1)

	var base_hit_count = 3
	if w_lvl <= 5:
		base_hit_count = 3 + (w_lvl - 1)
	elif w_lvl == 6:
		base_hit_count = 10
	elif w_lvl >= 7:
		base_hit_count = 15 # زنجیره همزمان به ۱۵ زامبی!

	var hit_count = min(base_hit_count, valid_enemies.size())
	for i in range(hit_count):
		if is_instance_valid(valid_enemies[i]):
			valid_enemies[i].take_damage(get_actual_damage())
			
			# زنجیره رعد و برق سطح ۶ به بالا دشمنان را کامل متوقف (Freeze/Stun) می‌کند
			if w_lvl >= 6 and "speed_multiplier" in valid_enemies[i]:
				valid_enemies[i].apply_slow(0.0, 1.2)
				
			# زنجیره رعد و برق سطح ۷: خشم خدای رعد - بارش صاعقه آسمانی با دمیج بسیار بالا
			if w_lvl >= 7:
				valid_enemies[i].take_damage(75.0)
