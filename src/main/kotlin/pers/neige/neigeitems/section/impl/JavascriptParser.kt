package pers.neige.neigeitems.section.impl

import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import pers.neige.neigeitems.manager.HookerManager.papi
import pers.neige.neigeitems.manager.ScriptManager
import pers.neige.neigeitems.section.SectionParser
import pers.neige.neigeitems.utils.SectionUtils.parseSection

// js节点解析器
object JavascriptParser : SectionParser() {
    override val id: String = "js"

    override fun onRequest(
        data: ConfigurationSection,
        cache: HashMap<String, String>?,
        player: OfflinePlayer?,
        sections: ConfigurationSection?
    ): String? {
        data.getString("path")?.let {
            val array = it.split("::")
            val path = array[0]
            val func = array[1]
            val map = HashMap<String, Any>()
            player?.let {
                map["player"] = player
                map["papi"] = java.util.function.Function<String, String> { string -> papi(player, string) }
            }
            map["vars"] = java.util.function.Function<String, String> { string -> string.parseSection(cache, player, sections) }
            return ScriptManager.compiledScripts[path]?.invokeFunction(func, map, args = data.getStringList("args").toTypedArray())?.toString()?.parseSection(cache, player, sections)
        }
        return null
    }

    override fun onRequest(
        args: List<String>,
        cache: HashMap<String, String>?,
        player: OfflinePlayer?,
        sections: ConfigurationSection?
    ): String {
        val data = YamlConfiguration()
        if (args.isNotEmpty()) data.set("path", args[0])
        val param = args.drop(1)
        if (param.isNotEmpty()) data.set("args", param)
        return onRequest(data, cache, player, sections) ?: "<$id::${args.joinToString("_")}>"
    }
}