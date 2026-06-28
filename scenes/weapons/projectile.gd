class_name Projectile
extends Area2D

var speed: float = 300.0
var damage: float = 15.0
var direction: Vector2 = Vector2.ZERO
var penetration_count: int = 1
var life_time: float = 4.0
var explosion_radius: float = 0.0

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	queue_redraw()
	
	var timer = get_tree().create_timer(life_time)
	timer.timeout.connect(queue_free)

func _physics_process(delta: float) -> void:
	if direction != Vector2.ZERO:
		global_position += direction * speed * delta

func _on_body_entered(body: Node2D) -> void:
	if body.is_in_group("enemies") and body.has_method("take_damage"):
		if explosion_radius > 0:
			_explode()
		else:
			body.take_damage(damage)
			penetration_count -= 1
			if penetration_count <= 0:
				queue_free()

func _explode() -> void:
	for enemy in get_tree().get_nodes_in_group("enemies"):
		if global_position.distance_to(enemy.global_position) <= explosion_radius:
			enemy.take_damage(damage)
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
