extends Node2D

const BASE_RADIUS = 80.0
const KNOB_RADIUS = 32.0

var joystick_active: bool = false
var joystick_start: Vector2 = Vector2.ZERO
var joystick_dir: Vector2 = Vector2.ZERO

func _ready() -> void:
	EventBus.joystick_updated.connect(_on_joystick_updated)

func _on_joystick_updated(active: bool, start_pos: Vector2, direction: Vector2) -> void:
	joystick_active = active
	joystick_start = start_pos
	joystick_dir = direction
	queue_redraw()

func _draw() -> void:
	if !joystick_active:
		return
	draw_circle(joystick_start, BASE_RADIUS, Color(1, 1, 1, 0.12))
	draw_arc(joystick_start, BASE_RADIUS, 0, TAU, 48, Color(1, 1, 1, 0.35), 2.5)
	var handle_pos = joystick_start + joystick_dir * 35.0
	draw_circle(handle_pos, KNOB_RADIUS, Color(1, 1, 1, 0.55))
	draw_arc(handle_pos, KNOB_RADIUS, 0, TAU, 24, Color(1, 1, 1, 0.9), 2.0)
