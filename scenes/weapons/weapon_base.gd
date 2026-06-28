class_name WeaponBase
extends Node2D

@export var weapon_config: WeaponData

@onready var fire_timer: Timer = $FireTimer

var target_enemy: CharacterBody2D = null

const MAX_PROJECTILES = 40

func _ready() -> void:
	if weapon_config:
		setup_weapon()

func setup_weapon() -> void:
	fire_timer.wait_time = 1.0 / weapon_config.fire_rate
	fire_timer.start()

func _on_fire_timer_timeout() -> void:
	if weapon_config and weapon_config.weapon_id == "lightning_chain":
		_chain_lightning()
		return
	
	target_enemy = get_nearest_enemy()
	if target_enemy:
		shoot_at_target(target_enemy)

func get_nearest_enemy() -> CharacterBody2D:
	var enemies = get_tree().get_nodes_in_group("enemies")
	var nearest: CharacterBody2D = null
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
	
	if get_tree().get_nodes_in_group("projectiles").size() >= MAX_PROJECTILES:
		return
	
	var proj_scene = preload("res://scenes/weapons/projectile.tscn")
	var proj_instance = proj_scene.instantiate() as Projectile
	
	proj_instance.global_position = global_position
	proj_instance.direction = (enemy.global_position - global_position).normalized()
	
	if weapon_config:
		proj_instance.damage = weapon_config.base_damage
		proj_instance.speed = weapon_config.projectile_speed
		proj_instance.penetration_count = weapon_config.penetration_count
		if weapon_config.weapon_id == "fireball":
			proj_instance.explosion_radius = 80.0
	
	proj_instance.add_to_group("projectiles")
	get_tree().current_scene.add_child(proj_instance)

func _chain_lightning() -> void:
	var enemies = get_tree().get_nodes_in_group("enemies")
	enemies.sort_custom(func(a, b):
		return global_position.distance_to(a.global_position) < global_position.distance_to(b.global_position)
	)
	var hit_count = min(3, enemies.size())
	for i in range(hit_count):
		if is_instance_valid(enemies[i]):
			enemies[i].take_damage(weapon_config.base_damage)
