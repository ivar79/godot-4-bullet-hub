class_name WeaponBase
extends Node2D

# -------------------------------------------------------------
# WeaponBase - منطق پایه شلیک سلاح‌ها
# نقش: فعال‌سازی تایمر شلیک خودکار، یافتن نزدیک‌ترین دشمن در شعاع حمله
# و شلیک پرتابه به سمت هدف با استفاده از اطلاعات WeaponData.
# -------------------------------------------------------------

@export var weapon_config: WeaponData

@onready var fire_timer: Timer = $FireTimer

var target_enemy: CharacterBody2D = null

func _ready() -> void:
	if weapon_config:
		setup_weapon()

func setup_weapon() -> void:
	# تنظیم طول زمان شلیک بر اساس سرعتی که تراز به سلاح می‌دهد
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
		if is_instance_valid(enemy):
			var dist = global_position.distance_to(enemy.global_position)
			if dist < min_dist:
				min_dist = dist
				nearest = enemy
	return nearest

func shoot_at_target(enemy: CharacterBody2D) -> void:
	# اینجا پرتابه (Projectile) از روی صحنهweapon_config.weapon_scene ساخته می‌شود
	# و راستای شلیک به سمت دشمن متغیر می‌گردد.
	pass
