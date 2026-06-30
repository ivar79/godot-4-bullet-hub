extends Node

@warning_ignore("unused_signal")

# -------------------------------------------------------------
# EventBus - اتولود سیگنال‌های عمومی بازی (Event Bus Pattern)
# نقش: این کلاس به عنوان یک مرکز هدایت رویدادها عمل می‌کند تا بخش‌های
# مختلف بازی (بازیکن، دشمنان، سلاح‌ها و رابط کاربری) بدون وابستگی مستقیم
# به یکدیگر با هم ارتباط برقرار کنند. کاملاً مناسب برای بازی‌های Bullet Heaven.
# -------------------------------------------------------------

# سیگنال‌های مربوط به بازیکن
signal player_spawned(player_node: CharacterBody2D)
signal player_health_changed(current_health: float, max_health: float)
signal player_died
signal player_level_up(new_level: int)
signal xp_collected(amount: int, current_xp: int, next_level_xp: int)

# سیگنال‌های مربوط به دشمنان
signal enemy_spawned(enemy_node: CharacterBody2D)
signal enemy_died(enemy_node: CharacterBody2D, score_value: int, xp_value: int)
signal final_boss_spawned

# سیگنال‌های مربوط به لول و موج‌ها
signal wave_started(wave_number: int)
signal wave_completed(wave_number: int)

# سیگنال‌های مربوط به مراحل
signal stage_completed(stage_number: int)
signal stage_started(stage_number: int)

# سیگنال‌های مربوط به گیم‌پلی عمومی
signal game_started
signal game_paused(is_paused: bool)
signal game_over(final_score: int, time_survived: float)
signal show_announcement(text: String, color: Color)

# سیگنال جوی‌استیک لمسی موبایل
signal joystick_updated(active: bool, start_pos: Vector2, current_dir: Vector2)
