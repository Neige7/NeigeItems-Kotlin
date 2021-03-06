package pers.neige.neigeitems.item

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import pers.neige.neigeitems.manager.ActionManager
import taboolib.module.nms.ItemTag

// 用于记录物品的物品动作信息
class ItemAction(val id: String, val config: ConfigurationSection) {
    // 物品消耗信息
    val consume: ConfigurationSection? = config.getConfigurationSection("consume")

    // 物品使用冷却
    val cooldown = config.getLong("cooldown", 0)

    // 冷却组ID
    val group = config.getString("group", id)

    // 使用物品左键交互
    val left = config.get("left")

    // 使用物品右键交互
    val right = config.get("right")

    // 使用物品左键或右键交互
    val all = config.get("all")

    // 食用或饮用物品
    val eat = config.get("eat")

    // 丢弃物品
    val drop = config.get("drop")

    // 捡起物品
    val pick = config.get("pick")

    // 运行某个动作
    fun run(player: Player, action: Any?, itemTag: ItemTag?) {
        action?.let {
            when (action) {
                is String -> ActionManager.runAction(player, action, itemTag)
                is List<*> -> ActionManager.runAction(player, action.map { value -> value.toString() }, itemTag)
                else -> return
            }
        }
    }
}