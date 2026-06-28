extends CanvasLayer

@onready var hp_bar: ProgressBar = $Control/HPBar
@onready var xp_bar: ProgressBar = $Control/XPBar
@onready var score_label: Label = $Control/ScoreLabel
@onready var level_label: Label = $Control/LevelLabel
@onready var timer_label: Label = $Control/TimerLabel
@onready var pause_btn: Button = $Control/PauseBtn
@onready var pause_overlay: ColorRect = $Control/PauseOverlay
@onready var pause_panel: Panel = $Control/PausePanel
@onready var resume_btn: Button = $Control/PausePanel/VBoxContainer/ResumeBtn
@onready var menu_btn: Button = $Control/PausePanel/VBoxContainer/MenuBtn

func _ready() -> void:
	EventBus.player_health_changed.connect(_on_player_health_changed)
	EventBus.xp_collected.connect(_on_xp_collected)
	EventBus.player_level_up.connect(_on_player_level_up)
	
	score_label.text = "Score: 0"
	level_label.text = "Lvl: 1"
	
	pause_btn.pressed.connect(_on_pause_pressed)
	resume_btn.pressed.connect(_on_resume_pressed)
	menu_btn.pressed.connect(_on_menu_pressed)

func _process(_delta: float) -> void:
	if GameManager.is_game_active:
		timer_label.text = format_time(GameManager.time_elapsed)
		score_label.text = "Score: " + str(GameManager.current_score)

func format_time(time_in_seconds: float) -> String:
	var minutes = int(time_in_seconds) / 60
	var seconds = int(time_in_seconds) % 60
	return "%02d:%02d" % [minutes, seconds]

func _on_player_health_changed(current: float, max_val: float) -> void:
	hp_bar.max_value = max_val
	hp_bar.value = current

func _on_xp_collected(_amt: int, current: int, next_lvl_xp: int) -> void:
	xp_bar.max_value = next_lvl_xp
	xp_bar.value = current

func _on_player_level_up(new_level: int) -> void:
	level_label.text = "Lvl: " + str(new_level)

func _on_pause_pressed() -> void:
	pause_overlay.visible = true
	pause_panel.visible = true
	GameManager.pause_game(true)

func _on_resume_pressed() -> void:
	pause_overlay.visible = false
	pause_panel.visible = false
	GameManager.pause_game(false)

func _on_menu_pressed() -> void:
	get_tree().paused = false
	get_tree().change_scene_to_file("res://scenes/ui/main_menu.tscn")
