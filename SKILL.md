---
name: godot-bullet-hub-conventions
description: Project-specific conventions for the "godot-4-bullet-hub" offline mobile bullet-heaven game (Godot 4, GDScript), inspired by Survivor.io / Vampire Survivors. ALWAYS consult this skill whenever reading, writing, editing, debugging, or reviewing any .gd or .tscn file in this project, whenever the user asks to add a new weapon/enemy/upgrade/feature to this game, or whenever the user mentions "bullet hub", "bullet-heaven", "survivors-like game", or this specific project by name. Also use when exporting the project to Android APK or when diagnosing why the game won't run/move/render correctly in the Godot editor.
---

# Godot 4 Bullet-Heaven Project Conventions

این اسکیل دانش معماری و قراردادهای پروژه‌ی بازی موبایل آفلاین `godot-4-bullet-hub` رو نگه می‌داره، تا هر بار که روی این پروژه کار می‌کنیم، از صفر کشف نشه.

## معرفی پروژه
- موتور: **Godot 4**, زبان: **GDScript** (نه C#)
- ژانر: bullet-heaven / survivors-like، کاملاً **آفلاین** (بدون سرور، بدون دیتابیس آنلاین)
- هدف نهایی: خروجی **APK** برای اندروید
- گرافیک فعلی فقط placeholder هندسی است (دایره/مربع با `draw_circle`/`draw_rect`) — این عمدی است؛ گرافیک نهایی بعداً با asset pack یا پیکسل آرت جایگزین می‌شود. هیچ‌وقت پیشنهاد نده که AI خودش اسپرایت/انیمیشن واقعی بسازد.

## ساختار پوشه‌ها (همیشه همین الگو را حفظ کن)
```
autoload/        -> Autoload های سراسری: event_bus.gd, game_manager.gd, save_manager.gd, enemy_pool.gd
resources/        -> Resource های سفارشی data-driven: character_data.gd, weapon_data.gd
scenes/player/    -> player.gd + player.tscn
scenes/enemies/   -> enemy_base.gd + enemy_base.tscn
scenes/weapons/   -> weapon_base.gd/.tscn, projectile.gd/.tscn
scenes/items/     -> xp_gem.gd/.tscn
scenes/ui/        -> hud.gd/.tscn, level_up_panel.gd/.tscn, game_over.gd/.tscn
scenes/levels/    -> main_level.gd/.tscn (صحنه اصلی که project.godot به آن اشاره می‌کند)
scripts/          -> اسکریپت‌های غیر صحنه‌ای: mobile_input.gd, enemy_spawner.gd
```
هیچ فایلی را خارج از این الگو نساز؛ اگر چیز جدیدی لازم شد، آن را زیر مناسب‌ترین پوشه‌ی موجود بگذار.

## الگوهای معماری ثابت پروژه
1. **ارتباط Signal-محور از طریق EventBus**: هیچ نودی مستقیماً متد نود دیگر را صدا نمی‌زند؛ همه‌چیز با `EventBus.emit_signal(...)` و اتصال در `_ready()` انجام می‌شود. هر سیگنال جدید باید ابتدا در `autoload/event_bus.gd` با `signal` تعریف شود — وگرنه `emit_signal` خطای ران‌تایم می‌دهد.
2. **داده‌محور بودن (Data-Driven)**: مشخصات کاراکتر/سلاح در `Resource`های `CharacterData` و `WeaponData` با `@export` تعریف می‌شوند، نه hardcode داخل اسکریپت رفتار.
3. **Object Pooling برای دشمنان**: هرگز از `queue_free()`/`instantiate()` مداوم برای دشمن استفاده نکن. همیشه از `EnemyPool` (autoload) با `get_enemy()` / `enemy.return_to_pool()` استفاده کن.
4. **کامنت‌گذاری**: کامنت‌های توضیحی کد به **فارسی** نوشته می‌شوند؛ نام متغیر/تابع/سیگنال به انگلیسی (snake_case طبق قرارداد GDScript).
5. **Pause-aware UI**: هر پنل UI که هنگام `get_tree().paused = true` باید قابل کلیک بماند (مثل `level_up_panel`, `game_over`)، باید `process_mode` آن (و توصیه می‌شود فرزندانش) را روی `PROCESS_MODE_ALWAYS` (مقدار 3) تنظیم کنی، وگرنه دکمه‌ها واکنشی نمی‌دهند.

## دام‌های شناخته‌شده (قبلاً این اشتباهات رخ داده‌اند — تکرار نکن)
- **`CanvasLayer` قابلیت `_draw()` / `queue_redraw()` ندارد** چون از `CanvasItem` ارث‌بری نمی‌کند. هر منطق رسم سفارشی (مثل جوی‌استیک لمسی) باید روی یک نود فرزند از نوع `Node2D` یا `Control` پیاده شود، نه مستقیم روی ریشه‌ی `CanvasLayer` در `hud.tscn`.
- **Input Map باید در `project.godot` تعریف شود**: اکشن‌هایی مثل `move_left/right/up/down` که در `player.gd` با `Input.get_vector(...)` استفاده می‌شوند، باید در بخش `[input]` پروژه موجود باشند وگرنه حرکت اصلاً کار نمی‌کند.
- **`scenes/levels/main_level.tscn` باید همیشه وجود داشته باشد** و دقیقاً همان مسیری باشد که `run/main_scene` در `project.godot` به آن اشاره می‌کند، وگرنه بازی اصلاً اجرا نمی‌شود.
- **Collision Layer/Mask باید عمداً هماهنگ باشند**: Player روی layer 2، Enemy روی layer 3 (مقدار 4)، Projectile روی layer 4 (مقدار 8) با mask=4 برای برخورد با دشمن. قبل از افزودن نود فیزیکی جدید، لایه‌بندی فعلی را در `player.tscn`, `enemy_base.tscn`, `projectile.tscn` چک کن تا تداخل ایجاد نشود.
- **این پروژه هرگز نباید فایل‌های Android/Gradle (`build.gradle.kts`, `settings.gradle.kts`, پوشه‌ی `app/`, `local.properties`) داشته باشد.** این‌ها زباله‌ی یک تولید اشتباه (اپ Kotlin/Jetpack Compose به‌جای پروژه‌ی واقعی Godot) بودند. خروجی APK واقعی Godot فقط از طریق `export_presets.cfg` + نصب Android Build Template داخل خود ادیتور Godot ممکن است، نه Gradle دستی.

## نکات تست و اجرا
- این پروژه فقط داخل **خود نرم‌افزار Godot 4 Editor** قابل اجرا و تست است (با F5). هیچ ابزار AI (چه Claude، چه Gemini، چه هرکدام) نمی‌تواند بازی را پیش‌نمایش/اجرا کند — همیشه به کاربر یادآوری کن که باید در ادیتور تست کند.
- برای تست جوی‌استیک لمسی با موس روی دسکتاپ، باید `input_devices/pointing/emulate_touch_from_mouse=true` در `project.godot` فعال باشد.
- کنترل کیبورد (WASD) همیشه باید به‌عنوان fallback دسکتاپ حفظ شود، حتی اگر تمرکز اصلی روی موبایل است.

## وقتی قابلیت جدیدی اضافه می‌کنی
1. اول الگوهای بالا (Signal از EventBus، Resource برای داده، Pooling برای موجودیت‌های زیاد) را اعمال کن.
2. کامنت فارسی توضیحی در ابتدای فایل و بالای توابع پیچیده بگذار.
3. اگر سیگنال جدید لازم است، اول آن را در `event_bus.gd` تعریف کن.
4. اگر نود فیزیکی جدید اضافه می‌کنی، collision_layer/mask را آگاهانه و هماهنگ با جدول بالا تنظیم کن.
5. در پایان، یک خلاصه کوتاه از فایل‌های تغییریافته/جدید بده تا کاربر بداند دقیقاً چه چیزی عوض شده.
