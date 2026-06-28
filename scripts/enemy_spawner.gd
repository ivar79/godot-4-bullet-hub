class_name EnemySpawner
extends Node2D

@onready var spawn_timer: Timer = Timer.new()

var player = null
var current_wave: int = 1
var spawn_interval: float = 1.5
var enemies_per_spawn: int = 1
var wave_duration: float = 25.0
var wave_timer: float = 0.0

func _ready() -> void:
	add_child(spawn_timer)
	spawn_timer.wait_time = spawn_interval
	spawn_timer.timeout.connect(_on_spawn_timer_timeout)
	spawn_timer.start()
	
	var players = get_tree().get_nodes_in_group("player")
	if players.size() > 0:
		player = players[0] as Player
	
	EventBus.player_spawned.connect(_on_player_spawned)
	EventBus.wave_started.emit(current_wave)

func _process(delta: float) -> void:
	if !GameManager.is_game_active:
		return
	
	wave_timer += delta
	if wave_timer >= wave_duration:
		_advance_wave()

func _advance_wave() -> void:
	wave_timer = 0.0
	current_wave += 1
	spawn_interval = max(0.4, 1.5 - (current_wave * 0.15))
	spawn_timer.wait_time = spawn_interval
	enemies_per_spawn = 1 + int(current_wave / 3)
	
	if current_wave % 5 == 0:
		await get_tree().create_timer(1.0).timeout
		_spawn_boss()
	
	EventBus.wave_completed.emit(current_wave - 1)
	EventBus.wave_started.emit(current_wave)

func _spawn_boss() -> void:
	if player == null or !is_instance_valid(player):
		return
	var boss = EnemyPool.get_enemy()
	if boss == null:
		return
	boss.max_health = 500.0 + current_wave * 60
	boss.current_health = boss.max_health
	boss.speed = 55.0
	boss.damage = 40.0 + current_wave * 4
	boss.score_value = 500
	boss.xp_value = 150
	boss.scale = Vector2(2.5, 2.5)
	boss.enemy_color = Color(0.6, 0.0, 0.8)
	var angle = randf() * TAU
	boss.global_position = player.global_position + Vector2(cos(angle), sin(angle)) * 520.0
	if boss.get_parent():
		boss.get_parent().remove_child(boss)
	get_parent().add_child(boss)

func _on_spawn_timer_timeout() -> void:
	if GameManager.is_game_active and player and is_instance_valid(player):
		for i in range(enemies_per_spawn):
			spawn_enemy()

func spawn_enemy() -> void:
	var enemy = EnemyPool.get_enemy()
	if enemy == null:
		return
	
	var spawn_radius = randf_range(500.0, 600.0)
	var angle = randf() * TAU
	var spawn_pos = player.global_position + Vector2(cos(angle), sin(angle)) * spawn_radius
	
	enemy.global_position = spawn_pos
	
	_configure_enemy_for_wave(enemy)
	
	if enemy.get_parent():
		enemy.get_parent().remove_child(enemy)
	get_parent().add_child(enemy)

func _configure_enemy_for_wave(enemy) -> void:
	var roll = randi() % 10
	var type: String
	if current_wave < 3:
		type = "zombie"
	elif roll < 5:
		type = "zombie"
	elif roll < 8:
		type = "runner"
	else:
		type = "tank"
	
	match type:
		"zombie":
			enemy.speed = 110.0 + current_wave * 5
			enemy.max_health = 30.0 + current_wave * 8
			enemy.damage = 10.0 + current_wave * 2
			enemy.enemy_color = Color(0.9, 0.2, 0.2)
			enemy.scale = Vector2.ONE
		"runner":
			enemy.speed = 220.0 + current_wave * 8
			enemy.max_health = 15.0 + current_wave * 4
			enemy.damage = 6.0 + current_wave * 1.5
			enemy.enemy_color = Color(1.0, 0.5, 0.1)
			enemy.scale = Vector2(0.75, 0.75)
		"tank":
			enemy.speed = 55.0 + current_wave * 2
			enemy.max_health = 150.0 + current_wave * 25
			enemy.damage = 25.0 + current_wave * 5
			enemy.enemy_color = Color(0.4, 0.1, 0.8)
			enemy.scale = Vector2(1.8, 1.8)
	
	enemy.current_health = enemy.max_health

func _on_player_spawned(player_node: CharacterBody2D) -> void:
	player = player_node
