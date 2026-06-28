extends CanvasLayer

@onready var option1_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option1
@onready var option2_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option2
@onready var option3_btn: Button = $Control/Panel/MarginContainer/VBoxContainer/Option3

var ALL_UPGRADES = [
	{"title": "Speed Boost",      "type": "speed",      "value": 1.20},
	{"title": "Damage Up",        "type": "damage",     "value": 1.30},
	{"title": "Heal",             "type": "health",     "value": 0.50},
	{"title": "Fire Rate",        "type": "fire_rate",  "value": 1.25},
	{"title": "Defense",          "type": "armor",      "value": 5.0},
	{"title": "New Weapon",       "type": "weapon",     "value": 0.0},
	{"title": "Gem Magnet",       "type": "magnet",     "value": 1.5},
	{"title": "Projectile Speed", "type": "proj_speed", "value": 1.20},
]

func _ready() -> void:
	get_tree().paused = true
	ALL_UPGRADES.shuffle()
	var chosen = ALL_UPGRADES.slice(0, 3)
	_setup_button(option1_btn, chosen[0])
	_setup_button(option2_btn, chosen[1])
	_setup_button(option3_btn, chosen[2])

func _setup_button(btn: Button, upgrade: Dictionary) -> void:
	btn.text = upgrade["title"]
	btn.pressed.connect(func(): _apply_upgrade(upgrade))

func _apply_upgrade(upgrade: Dictionary) -> void:
	var player = get_tree().get_first_node_in_group("player")
	if player == null:
		_resume()
		return
	match upgrade["type"]:
		"speed":
			player.stats.base_speed *= upgrade["value"]
		"damage":
			for w in player.equipped_weapons:
				w.base_damage *= upgrade["value"]
		"health":
			player.heal(player.stats.max_health * upgrade["value"])
		"fire_rate":
			for w in player.equipped_weapons:
				w.fire_rate *= upgrade["value"]
		"armor":
			player.stats.defense += upgrade["value"]
		"magnet":
			for gem in get_tree().get_nodes_in_group("gems"):
				gem.magnet_radius *= upgrade["value"]
		"proj_speed":
			for w in player.equipped_weapons:
				w.projectile_speed *= upgrade["value"]
	_resume()

func _resume() -> void:
	get_tree().paused = false
	queue_free()
