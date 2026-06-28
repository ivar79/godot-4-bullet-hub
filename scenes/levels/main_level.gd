extends Node2D

const XPGemScene: PackedScene = preload("res://scenes/items/xp_gem.tscn")
const LevelUpScene: PackedScene = preload("res://scenes/ui/level_up_panel.tscn")
const GameOverScene: PackedScene = preload("res://scenes/ui/game_over.tscn")

func _ready() -> void:
	Engine.max_fps = 60
	
	EventBus.enemy_died.connect(_on_enemy_died)
	EventBus.player_level_up.connect(_on_player_level_up)
	EventBus.game_over.connect(_on_game_over)
	
	GameManager.start_game()

func _on_enemy_died(enemy_node: CharacterBody2D, _score: int, xp_value: int) -> void:
	var gem_instance = XPGemScene.instantiate() as XPGem
	gem_instance.xp_value = xp_value
	gem_instance.global_position = enemy_node.global_position
	call_deferred("add_child", gem_instance)

func _on_player_level_up(_new_level: int) -> void:
	var lvlup_instance = LevelUpScene.instantiate()
	add_child(lvlup_instance)

func _on_game_over(_final_score: int, _time_survived: float) -> void:
	get_tree().paused = true
	var gameover_instance = GameOverScene.instantiate()
	add_child(gameover_instance)
