extends ColorRect

func _draw() -> void:
	var grid_size = 100
	var start_x = int(position.x) - (int(position.x) % grid_size)
	var start_y = int(position.y) - (int(position.y) % grid_size)
	
	for x in range(start_x, int(position.x + size.x), grid_size):
		draw_line(Vector2(x, position.y), Vector2(x, position.y + size.y), Color(0.18, 0.22, 0.18, 0.4), 1.0)
	for y in range(start_y, int(position.y + size.y), grid_size):
		draw_line(Vector2(position.x, y), Vector2(position.x + size.x, y), Color(0.18, 0.22, 0.18, 0.4), 1.0)
