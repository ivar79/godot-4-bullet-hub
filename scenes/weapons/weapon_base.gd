class_name WeaponBase
extends Node2D

# -------------------------------------------------------------
# WeaponBase - منطق پایه شلیک سلاح‌ها (طراحی غیرمسدودکننده و بهینه)
# نقش: فعال‌سازی تایمر شلیک خودکار، یافتن نزدیک‌ترین دشمن در شعاع حمله،
# تولید و تزریق مشحصات آماری سلاح به پرتابه (Projectile) شلیک شده.
# -------------------------------------------------------------

@export var weapon_config: WeaponData

@onready var fire_timer: Timer = $FireTimer

var target_enemy: CharacterBody2D = null

func _ready() -> void:
	if weapon_config:
		setup_weapon()

func setup_weapon() -> void:
	# تنظیم زمان شلیک بر اساس ویژگی ریسورس
	fire_timer.wait_time = 1.0 / weapon_config.fire_rate
	fire_timer.start()

func _on_fire_timer_timeout() -> void:
	target_enemy = get_nearest_enemy()
	if target_enemy:
		shoot_at_target(target_enemy)

func get_nearest_enemy() -> CharacterBody2D:
	var enemies = get_tree().get_nodes_in_group("enemies")
	var nearest: CharacterBody2D = null
	var min_dist: float = INF
	
	for enemy in enemies:
		if is_instance_valid(enemy) and enemy.visible: # فیلتر دشمنان فعال در صحنه
			var dist = global_position.distance_to(enemy.global_position)
			if dist < min_dist:
				min_dist = dist
				nearest = enemy
	return nearest

func shoot_at_target(enemy: CharacterBody2D) -> void:
	if !is_instance_valid(enemy):
		return
		
	# ایجاد گلوله و اتصال آن به صحنه اصلی بازی تا مستقل از جابجایی بازیکن حركت نماید
	var proj_scene = preload("res://scenes/weapons/projectile.tscn")
	var proj_instance = proj_scene.instantiate() as Projectile
	
	proj_instance.global_position = global_position
	proj_instance.direction = (enemy.global_position - global_position).normalized()
	
	if weapon_config:
		proj_instance.damage = weapon_config.base_damage
		proj_instance.speed = weapon_config.projectile_speed
		proj_instance.penetration_count = weapon_config.penetration_count
		
	get_tree().current_scene.add_child(proj_instance)
