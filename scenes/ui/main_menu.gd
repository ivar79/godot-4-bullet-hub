extends CanvasLayer

func _ready() -> void:
	$Control/HighScoreLabel.text = "Best: " + str(SaveManager.high_score)
	$Control/PlayButton.pressed.connect(_on_play)

func _on_play() -> void:
	get_tree().change_scene_to_file("res://scenes/levels/main_level.tscn")
