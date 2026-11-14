package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

/** Built-in translations: English, Ukrainian, Russian. */
public final class Lang {
	public static final String[] NAMES = {"English", "Українська", "Русский"};

	private static final Map<String, String[]> T = new HashMap<>();

	static {
		add("menu.fullbright", "Fullbright", "Повна яскравість", "Полная яркость");
		add("menu.zoom", "Zoom", "Зум", "Зум");
		add("menu.hud", "Info HUD", "Інфо-HUD", "Инфо-HUD");
		add("menu.watermark", "Watermark", "Ватермарк", "Ватермарк");
		add("menu.autoswap", "AutoSwap", "Автосвап", "Автосвап");
		add("menu.pet", "Plankton Pet", "Пет Планктон", "Пет Планктон");
		add("menu.cosmetics", "Cosmetics", "Косметика", "Косметика");
		add("menu.language", "Language", "Мова", "Язык");
		add("menu.talisman", "Talisman", "Талісман", "Талисман");
		add("menu.kwami", "Kwami (Nooroo)", "Квамі (Нуру)", "Квами (Нууру)");
		add("menu.darkwings", "Dark Wings", "Темні крила", "Тёмные крылья");
		add("btn.open", "Open", "Відкрити", "Открыть");

		add("tal.title", "Butterfly Talisman", "Талісман метелика", "Талисман бабочки");
		add("tal.kwami", "Kwami", "Квамі", "Квами");
		add("tal.kwami.name", "Nooroo", "Нуру", "Нууру");
		add("tal.kwami.empty", "Empty", "Порожньо", "Пусто");
		add("tal.add", "Add kwami", "Додати квамі", "Добавить квами");
		add("tal.remove", "Remove kwami", "Прибрати квамі", "Убрать квами");
		add("tal.phrase", "Phrase", "Фраза", "Фраза");
		add("tal.placeholder", "Type the phrase...", "Введіть фразу...", "Введите фразу...");
		add("tal.transform", "Transform", "Перетворитись", "Превратиться");
		add("tal.detransform", "Detransform", "Скинути перетворення", "Сбросить превращение");
		add("tal.msg.hint", "Add the kwami and say the phrase", "Додай квамі та скажи фразу", "Добавь квами и скажи фразу");
		add("tal.msg.needkwami", "Add the kwami first!", "Спершу додай квамі!", "Сначала добавь квами!");
		add("tal.msg.wrong", "Wrong phrase", "Неправильна фраза", "Неверная фраза");
		add("tal.msg.ok", "Transformed!", "Перетворення завершено!", "Превращение завершено!");
		add("tal.msg.off", "Transformation ended", "Перетворення скасовано", "Превращение отменено");

		add("common.on", "ON", "УВІМК", "ВКЛ");
		add("common.off", "OFF", "ВИМК", "ВЫКЛ");

		add("btn.wheel", "Wheel", "Коло", "Круг");
		add("cosmetic.cape", "Cape", "Плащ", "Плащ");
		add("cosmetic.wings", "Wings", "Крила", "Крылья");

		add("hud.facing", "Facing", "Напрямок", "Направление");
		add("hud.autoswap", "AutoSwap", "Автосвап", "Автосвап");
		add("hud.none", "none", "немає", "нет");

		add("dir.north", "north", "північ", "север");
		add("dir.south", "south", "південь", "юг");
		add("dir.east", "east", "схід", "восток");
		add("dir.west", "west", "захід", "запад");
		add("dir.up", "up", "вгору", "вверх");
		add("dir.down", "down", "вниз", "вниз");

		add("swap.title", "Auto Swap", "Автосвап", "Автосвап");
		add("swap.hint1",
				"Click an inventory item to add it",
				"Клік по предмету в інвентарі — додати",
				"Клик по предмету в инвентаре — добавить");
		add("swap.hint2",
				"LMB select | RMB clear | center = on/off",
				"ЛКМ вибрати | ПКМ очистити | центр = увімк./вимк.",
				"ЛКМ выбрать | ПКМ очистить | центр = вкл./выкл.");
	}

	private Lang() {}

	private static void add(String key, String en, String uk, String ru) {
		T.put(key, new String[]{en, uk, ru});
	}

	/** Current language index: the saved choice, or the game language if nothing was chosen yet. */
	public static int index() {
		int saved = VisualsConfig.language;
		if (saved >= 0 && saved < NAMES.length) return saved;
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client != null && client.options != null && client.options.language != null) {
				String code = client.options.language.toLowerCase();
				if (code.startsWith("uk")) return 1;
				if (code.startsWith("ru")) return 2;
			}
		} catch (Exception ignored) {
		}
		return 0;
	}

	public static void next() {
		VisualsConfig.language = (index() + 1) % NAMES.length;
		VisualsConfig.save();
	}

	public static String name() {
		return NAMES[index()];
	}

	public static String t(String key) {
		String[] v = T.get(key);
		return v == null ? key : v[index()];
	}
}
