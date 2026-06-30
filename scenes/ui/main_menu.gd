extends CanvasLayer

func _ready() -> void:
	# اطمینان حاصل می‌کنیم که درخت بازی متوقف (Pause) نیست تا دکمه‌ها و عناصر بازی کار کنند
	get_tree().paused = false
	process_mode = Node.PROCESS_MODE_ALWAYS
	
	$Control/HighScoreLabel.text = "بالاترین رکورد امتیاز: " + str(SaveManager.high_score)
	$Control/CompletedStagesLabel.text = "مراحل کامل شده جاری: " + str(SaveManager.max_completed_stage)
	
	var next_stage = SaveManager.max_completed_stage + 1
	var duration_str = "۵ دقیقه" if (next_stage % 5 == 0) else "۳ دقیقه"
	$Control/VBoxContainer/PlayButton.text = "⚔️ شروع مرحله " + str(next_stage) + " (" + duration_str + ") ⚔️"
	
	$Control/VBoxContainer/PlayButton.pressed.connect(_on_play_pressed)

func _on_play_pressed() -> void:
	get_tree().paused = false
	get_tree().change_scene_to_file("res://scenes/levels/main_level.tscn")

