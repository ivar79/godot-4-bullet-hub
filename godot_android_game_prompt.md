# Godot 4 Bullet Heaven — Fix & Complete for Android (Offline)

## Project Overview

I have a Godot 4 Bullet Heaven (Vampire Survivors-style) mobile template.
The project already has this architecture in place:

**Autoloads:** `EventBus`, `GameManager`, `SaveManager`, `EnemyPool`
**Scenes:** `Player`, `EnemyBase`, `WeaponBase`, `Projectile`, `XPGem`, `EnemySpawner`, `HUD`, `LevelUpPanel`, `GameOver`
**Scripts:** `MobileInput` (swipe joystick), `CharacterData` (Resource), `WeaponData` (Resource)
**Viewport:** 720×1280, portrait, canvas_items stretch, `emulate_touch_from_mouse=true`
**All visuals are drawn with `draw_circle` / `draw_rect` — no sprites yet**

The game must run fully offline on Android 7+ with no internet dependency whatsoever.

---

## CRITICAL BUGS TO FIX FIRST

### Bug 1 — Missing CollisionShape2D in every scene

No scene has a CollisionShape2D, so nothing collides. Add the following:

**player.tscn:**
```
CharacterBody2D (Player)
  ├── CollisionShape2D → CircleShape2D(radius=14)
  └── WeaponHolder (Node2D)
```

**enemy_base.tscn:**
```
CharacterBody2D (EnemyBase)
  └── CollisionShape2D → RectangleShape2D(size=Vector2(28,28))
```

**projectile.tscn:**
```
Area2D (Projectile)
  └── CollisionShape2D → CircleShape2D(radius=5)
      collision_layer = 8
      collision_mask  = 2
      monitoring = true
      monitorable = false
```

**xp_gem.tscn:**
```
Area2D (XPGem)
  └── CollisionShape2D → CircleShape2D(radius=8)
      collision_layer = 16
      collision_mask  = 4
```

Set collision layers in Project Settings:
```
Layer 1 = "world"    (value 1)
Layer 2 = "enemies"  (value 2)
Layer 3 = "player"   (value 4)
Layer 4 = "bullets"  (value 8)
Layer 5 = "gems"     (value 16)

Player:     layer=4,  mask=2|16
EnemyBase:  layer=2,  mask=4
Projectile: layer=8,  mask=2
XPGem:      layer=16, mask=4
```

### Bug 2 — Wrong renderer (crashes on mobile)

In `project.godot`, change:
```ini
[application]
config/features=PackedStringArray("4.3", "Mobile")

[rendering]
renderer/rendering_method="mobile"
renderer/rendering_method.mobile="gl_compatibility"
environment/defaults/default_clear_color=Color(0.08, 0.05, 0.12, 1)
```

### Bug 3 — EnemyPool orphans enemies before pooling

In `enemy_pool.gd`, `_ready()` must add enemies as children before pooling:
```gdscript
func _ready() -> void:
    for i in range(15):
        var enemy = enemy_scene.instantiate() as CharacterBody2D
        add_child(enemy)          # must be in scene tree
        enemy.visible = false
        enemy.process_mode = PROCESS_MODE_DISABLED
        pool.append(enemy)
```

### Bug 4 — Godot 4 signal syntax errors

Fix all signal connections throughout the project to use Godot 4 syntax:
```gdscript
# WRONG (Godot 3):
connect("signal_name", Callable(self, "method"))
emit_signal("signal_name", arg)
timer.connect("timeout", Callable(self, "method"))

# CORRECT (Godot 4):
signal_name.connect(method)
signal_name.emit(arg)
timer.timeout.connect(method)
```

### Bug 5 — XPGem uses wrong process function

`XPGem` is an `Area2D`, not a `CharacterBody2D`. Replace `_physics_process` with `_process` and move manually:
```gdscript
func _process(delta: float) -> void:
    if player and is_instance_valid(player):
        var dist = global_position.distance_to(player.global_position)
        if dist <= magnet_radius or is_magnetized:
            is_magnetized = true
            var dir = (player.global_position - global_position).normalized()
            global_position += dir * speed * delta
            speed = min(speed + 12.0, 800.0)
        if dist <= collect_radius:
            collect()
```

---

## GAMEPLAY SYSTEMS TO COMPLETE

### 1 — Camera Follow

Add to `main_level.gd`:
```gdscript
func _on_player_spawned(player_node: CharacterBody2D) -> void:
    var cam = Camera2D.new()
    cam.zoom = Vector2(0.9, 0.9)
    cam.position_smoothing_enabled = true
    cam.position_smoothing_speed = 8.0
    player_node.add_child(cam)
    cam.make_current()
```

### 2 — Three Weapons (add to existing system)

Keep the existing Magic Wand. Add two more weapons using the existing `WeaponData` resource and `WeaponBase` scene:

**Fireball** — area-of-effect explosion:
```gdscript
# In projectile.gd, add:
@export var explosion_radius: float = 0.0

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
```

**Lightning Chain** — hits 3 enemies instantly, no projectile:
```gdscript
# New weapon type in weapon_base.gd
func _chain_lightning() -> void:
    var enemies = get_tree().get_nodes_in_group("enemies")
    enemies.sort_custom(func(a, b):
        return global_position.distance_to(a.global_position) < global_position.distance_to(b.global_position)
    )
    var hit_count = min(3, enemies.size())
    for i in range(hit_count):
        if is_instance_valid(enemies[i]):
            enemies[i].take_damage(weapon_config.base_damage)
```

### 3 — Dynamic Level-Up Options

Replace hardcoded buttons in `level_up_panel.gd` with a random selection system:

```gdscript
const ALL_UPGRADES = [
    {"title": "⚡ Speed Boost",     "type": "speed",     "value": 1.20},
    {"title": "💥 Damage Up",       "type": "damage",    "value": 1.30},
    {"title": "❤️ Heal",            "type": "health",    "value": 0.50},
    {"title": "🔥 Fire Rate",       "type": "fire_rate", "value": 1.25},
    {"title": "🛡️ Defense",         "type": "armor",     "value": 5.0},
    {"title": "🪄 New Weapon",      "type": "weapon",    "value": 0.0},
    {"title": "🧲 Gem Magnet",      "type": "magnet",    "value": 1.5},
    {"title": "💨 Projectile Speed","type": "proj_speed","value": 1.20},
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
    var player = get_tree().get_first_node_in_group("player") as Player
    if player == null:
        _resume(); return
    match upgrade["type"]:
        "speed":      player.stats.base_speed *= upgrade["value"]
        "damage":
            for w in player.equipped_weapons:
                w.base_damage *= upgrade["value"]
        "health":     player.heal(player.stats.max_health * upgrade["value"])
        "fire_rate":
            for w in player.equipped_weapons:
                w.fire_rate *= upgrade["value"]
        "armor":      player.stats.defense += upgrade["value"]
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
```

### 4 — Enemy Variety (3 types)

Extend `EnemySpawner` and `EnemyBase` to support 3 enemy types:

Add to `enemy_base.gd`:
```gdscript
var enemy_color: Color = Color(0.9, 0.2, 0.2)

func _draw() -> void:
    draw_rect(Rect2(-14, -14, 28, 28), enemy_color, true)
    draw_rect(Rect2(-14, -14, 28, 28), Color.BLACK, false, 2.0)
```

Add to `enemy_spawner.gd`:
```gdscript
func _configure_enemy_for_wave(enemy: EnemyBase) -> void:
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
```

### 5 — Wave Boss every 5 waves

Add to `enemy_spawner.gd` inside `_advance_wave()`:
```gdscript
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
    if boss == null: return
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
    if boss.get_parent(): boss.get_parent().remove_child(boss)
    get_parent().add_child(boss)
```

---

## UI — COMPLETE THESE SCENES

### Main Menu (`scenes/ui/main_menu.tscn`)

Create this scene with the following node structure:
```
CanvasLayer
  └── Control (anchors: full rect)
        ├── ColorRect (full rect, color: Color(0.05,0.03,0.1,1))
        ├── Label "BULLET HEAVEN"
        │     (anchor: top-center, font_size: 52, outline)
        ├── Label (id: HighScoreLabel)
        │     text bound to SaveManager.high_score in _ready()
        ├── Button "▶  PLAY"
        │     (min_size: Vector2(220,64), pressed → load main_level)
        └── Button "🏆  BEST SCORES"
              (min_size: Vector2(220,64))
```

```gdscript
# main_menu.gd
extends CanvasLayer

func _ready() -> void:
    $Control/HighScoreLabel.text = "Best: " + str(SaveManager.high_score)
    $Control/PlayButton.pressed.connect(_on_play)

func _on_play() -> void:
    get_tree().change_scene_to_file("res://scenes/levels/main_level.tscn")
```

Set as main scene in `project.godot`:
```ini
run/main_scene="res://scenes/ui/main_menu.tscn"
```

### Virtual Joystick (`scenes/ui/joystick_visual.gd`)

Replace the existing placeholder with a full visual joystick drawn on-screen:
```gdscript
extends Control

const BASE_RADIUS  = 80.0
const KNOB_RADIUS  = 32.0
const ACTIVE_ZONE_X_MAX = 0.5  # left half of screen only

var base_pos: Vector2 = Vector2.ZERO
var knob_offset: Vector2 = Vector2.ZERO
var active_touch_idx: int = -1
var is_active: bool = false

signal direction_changed(dir: Vector2)
signal released

func _input(event: InputEvent) -> void:
    if event is InputEventScreenTouch:
        var norm_x = event.position.x / get_viewport_rect().size.x
        if event.pressed and norm_x < ACTIVE_ZONE_X_MAX and active_touch_idx == -1:
            active_touch_idx = event.index
            base_pos = event.position
            is_active = true
            queue_redraw()
        elif !event.pressed and event.index == active_touch_idx:
            active_touch_idx = -1
            is_active = false
            knob_offset = Vector2.ZERO
            queue_redraw()
            released.emit()
            direction_changed.emit(Vector2.ZERO)

    elif event is InputEventScreenDrag and event.index == active_touch_idx:
        var delta = event.position - base_pos
        var clamped = delta.limit_length(BASE_RADIUS)
        knob_offset = clamped
        queue_redraw()
        direction_changed.emit(clamped.normalized() if delta.length() > 10 else Vector2.ZERO)

func _draw() -> void:
    if !is_active: return
    draw_circle(base_pos, BASE_RADIUS, Color(1, 1, 1, 0.12))
    draw_arc(base_pos, BASE_RADIUS, 0, TAU, 48, Color(1, 1, 1, 0.35), 2.5)
    draw_circle(base_pos + knob_offset, KNOB_RADIUS, Color(1, 1, 1, 0.55))
    draw_arc(base_pos + knob_offset, KNOB_RADIUS, 0, TAU, 24, Color(1, 1, 1, 0.9), 2.0)
```

Add this node to `hud.tscn` and connect `direction_changed` to the player's `joystick_direction`.

### Pause Button + Pause Panel

Add to `hud.tscn`:
```
CanvasLayer
  └── Control
        └── Button "⏸" (id: PauseBtn)
              anchor: top-right
              custom_minimum_size: Vector2(56, 56)
              pressed → GameManager.pause_game(true)
              and show PausePanel
```

Pause panel (child of HUD CanvasLayer, hidden by default):
```
Panel (id: PausePanel, visible=false, process_mode=PROCESS_MODE_ALWAYS)
  └── VBoxContainer
        ├── Label "PAUSED"
        ├── Button "▶ Resume"  → GameManager.pause_game(false), hide panel
        └── Button "🏠 Main Menu" → get_tree().change_scene_to_file(main_menu)
```

---

## MOBILE PERFORMANCE — REQUIRED SETTINGS

Add to `project.godot`:
```ini
[physics]
2d/default_gravity=0
common/physics_ticks_per_second=30

[rendering]
renderer/rendering_method="mobile"
renderer/rendering_method.mobile="gl_compatibility"
```

Add to `main_level.gd` `_ready()`:
```gdscript
Engine.max_fps = 60
```

Cap active projectiles in `weapon_base.gd`:
```gdscript
const MAX_PROJECTILES = 40

func shoot_at_target(enemy: CharacterBody2D) -> void:
    if get_tree().get_nodes_in_group("projectiles").size() >= MAX_PROJECTILES:
        return
    # ... rest of existing code ...
    proj_instance.add_to_group("projectiles")
    get_tree().current_scene.add_child(proj_instance)
```

Remove group on free — add to `projectile.gd`:
```gdscript
func _notification(what: int) -> void:
    if what == NOTIFICATION_PREDELETE:
        remove_from_group("projectiles")
```

---

## ANDROID EXPORT SETTINGS

In Godot: **Project → Export → Add → Android**

```
Package Name:  com.yourstudio.bulletheavenmobile
Version Name:  1.0.0
Version Code:  1
Min SDK:       24   (Android 7.0)
Target SDK:    34
Renderer:      OpenGL ES 3 (Compatibility)
```

Required permissions: **VIBRATE only** (optional).
Do NOT add INTERNET — the game is offline.

`user://` path works on Android without any storage permission.

---

## IMPLEMENTATION ORDER (priority)

```
Priority 1 — Crashes / broken core
  [1] Add CollisionShape2D to all 4 scenes
  [2] Fix renderer to Mobile / GL Compatibility
  [3] Fix EnemyPool _ready() to add_child before pool
  [4] Fix all signal syntax to Godot 4 style
  [5] Fix XPGem to use _process instead of _physics_process

Priority 2 — Missing but essential
  [6] Camera2D follow player
  [7] Main Menu scene + set as main scene
  [8] Virtual Joystick visual (replace existing placeholder)
  [9] Pause button + pause panel in HUD

Priority 3 — Gameplay completeness
  [10] Dynamic level-up options (random from pool)
  [11] Fireball weapon (AoE explosion)
  [12] Lightning Chain weapon (instant multi-hit)
  [13] Enemy variety (Zombie / Runner / Tank)
  [14] Wave Boss every 5 waves

Priority 4 — Polish
  [15] Player rotation toward movement direction
  [16] Physics/FPS settings (30 ticks, 60fps cap)
  [17] Projectile count cap (MAX_PROJECTILES = 40)
  [18] Android Export configuration
```

---

## KEY RULES

- All code must be **Godot 4.3 GDScript** — no Godot 3 syntax
- Game must work **100% offline** — no HTTP requests, no ads SDK, no analytics
- Keep APK under **50 MB**
- Target minimum **30 FPS** on a mid-range Android device (Snapdragon 665 / Mali-G52)
- `SaveManager` uses `user://offline_save.dat` — do not change this path
- Do not add any new autoloads — use the existing 4 (EventBus, GameManager, SaveManager, EnemyPool)
- Do not use Vulkan features — GL Compatibility only
