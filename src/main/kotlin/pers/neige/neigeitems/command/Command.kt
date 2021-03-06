package pers.neige.neigeitems.command

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import pers.neige.neigeitems.NeigeItems.bukkitScheduler
import pers.neige.neigeitems.NeigeItems.plugin
import pers.neige.neigeitems.manager.*
import pers.neige.neigeitems.manager.ActionManager.runAction
import pers.neige.neigeitems.manager.ConfigManager.config
import pers.neige.neigeitems.manager.HookerManager.mythicMobsHooker
import pers.neige.neigeitems.manager.ItemManager.getItemStack
import pers.neige.neigeitems.manager.ItemManager.saveItem
import pers.neige.neigeitems.utils.ItemUtils.dropNiItem
import pers.neige.neigeitems.utils.ItemUtils.dropNiItems
import pers.neige.neigeitems.utils.PlayerUtils.giveItems
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getName
import taboolib.platform.BukkitAdapter
import taboolib.platform.util.giveItem
import taboolib.platform.util.hoverItem
import java.io.File
import java.util.*
import kotlin.math.ceil

@CommandHeader(name = "NeigeItems", aliases = ["ni"])
object Command {
    private val bukkitAdapter = BukkitAdapter()

    @CommandBody
    val main = mainCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        incorrectSender { sender, _ ->
            config.getString("Messages.onlyPlayer")?.let { sender.sendMessage(it) }
        }
        incorrectCommand { sender, _, _, _ ->
            help(sender)
        }
    }

    @CommandBody
    val test = subCommand {
        dynamic(commit = "item") {
            suggestion<Player>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            dynamic(optional = true, commit = "amount") {
                execute<Player> { sender, context, argument ->
                    submit(async = true) {
                        val time = Date().time
                        repeat(argument.toIntOrNull() ?: 1) {
                            getItemStack(context.argument(-1), sender)
                        }
                        println("??????: ${Date().time - time}ms")
                    }
                }
            }
        }
    }

    @CommandBody
    val action = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        dynamic(commit = "player") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            execute<CommandSender> { sender, _, _ ->
                submit(async = true) {
                    help(sender)
                }
            }
            dynamic(commit = "action") {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    arrayListOf("action")
                }
                execute<CommandSender> { _, context, argument ->
                    submit(async = true) {
                        Bukkit.getPlayerExact(context.argument(-1))?.let { player ->
                            runAction(player, argument)
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _, ->
            listCommandAsync(sender, 1)
        }
        dynamic(commit = "page") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                (1..ceil(ItemManager.itemAmount.toDouble()/config.getDouble("ItemList.ItemAmount")).toInt()).toList().map { it.toString() }
            }
            execute<CommandSender> { sender, _, argument ->
                listCommandAsync(sender, argument.toIntOrNull()?:1)
            }
        }
    }

    @CommandBody
    // ni get [??????ID] (??????) (??????????????????) (????????????) > ??????ID??????NI??????
    val get = subCommand {
        execute<Player> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ni get [??????ID]
        dynamic(commit = "item") {
            suggestion<Player>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            execute<Player> { sender, _, argument ->
                giveCommandAsync(sender, sender, argument, "1")
            }
            // ni get [??????ID] (??????)
            dynamic(optional = true, commit = "amount") {
                suggestion<Player>(uncheck = true) { _, _ ->
                    arrayListOf("amount")
                }
                execute<Player> { sender, context, argument ->
                    giveCommandAsync(sender, sender, context.argument(-1), argument)
                }
                // ni get [??????ID] (??????) (??????????????????)
                dynamic(optional = true, commit = "random") {
                    suggestion<Player>(uncheck = true) { _, _ ->
                        arrayListOf("true", "false")
                    }
                    execute<Player> { sender, context, argument ->
                        giveCommandAsync(sender, sender, context.argument(-2), context.argument(-1), argument)
                    }
                    // ni get [??????ID] (??????) (??????????????????) (????????????)
                    dynamic(optional = true, commit = "data") {
                        suggestion<Player>(uncheck = true) { _, _ ->
                            arrayListOf("data")
                        }
                        execute<Player> { sender, context, argument ->
                            giveCommandAsync(sender, sender, context.argument(-3), context.argument(-2), context.argument(-1), argument)
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    // ni give [??????ID] [??????ID] (??????) (??????????????????) (????????????) > ??????ID??????NI??????
    val give = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        dynamic(commit = "player") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                Bukkit.getOnlinePlayers().map { it.name }
            }
            execute<CommandSender> { sender, _, _ ->
                submit(async = true) {
                    help(sender)
                }
            }
            // ni give [??????ID] [??????ID]
            dynamic(commit = "item") {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    ItemManager.items.keys.toList()
                }
                execute<CommandSender> { sender, context, argument ->
                    giveCommandAsync(sender, Bukkit.getPlayerExact(context.argument(-1)), argument, "1")
                }
                // ni give [??????ID] [??????ID] (??????)
                dynamic(optional = true, commit = "amount") {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        arrayListOf("amount")
                    }
                    execute<CommandSender> { sender, context, argument ->
                        giveCommandAsync(sender, Bukkit.getPlayerExact(context.argument(-2)), context.argument(-1), argument)
                    }
                    // ni give [??????ID] [??????ID] (??????) (??????????????????)
                    dynamic(optional = true, commit = "random") {
                        suggestion<CommandSender>(uncheck = true) { _, _ ->
                            arrayListOf("true", "false")
                        }
                        execute<CommandSender> { sender, context, argument ->
                            giveCommandAsync(sender, Bukkit.getPlayerExact(context.argument(-3)), context.argument(-2), context.argument(-1), argument)
                        }
                        // ni give [??????ID] [??????ID] (??????) (??????????????????) (????????????)
                        dynamic(optional = true, commit = "data") {
                            suggestion<CommandSender>(uncheck = true) { _, _ ->
                                arrayListOf("data")
                            }
                            execute<CommandSender> { sender, context, argument ->
                                giveCommandAsync(sender, Bukkit.getPlayerExact(context.argument(-4)), context.argument(-3), context.argument(-2), context.argument(-1), argument)
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    // ni giveAll [??????ID] (??????) (??????????????????) (????????????) > ??????ID???????????????NI??????
    val giveAll = subCommand {
        execute<CommandSender> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ni giveAll [??????ID]
        dynamic(commit = "item") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            execute<CommandSender> { sender, _, argument ->
                giveAllCommandAsync(sender, argument, "1")
            }
            // ni giveAll [??????ID] (??????)
            dynamic(optional = true, commit = "amount") {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    arrayListOf("amount")
                }
                execute<CommandSender> { sender, context, argument ->
                    giveAllCommandAsync(sender, context.argument(-1), argument)
                }
                // ni giveAll [??????ID] (??????) (??????????????????)
                dynamic(optional = true, commit = "random") {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        arrayListOf("true", "false")
                    }
                    execute<CommandSender> { sender, context, argument ->
                        giveAllCommandAsync(sender, context.argument(-2), context.argument(-1), argument)
                    }
                    // ni giveAll [??????ID] (??????) (??????????????????) (????????????)
                    dynamic(optional = true, commit = "data") {
                        suggestion<CommandSender>(uncheck = true) { _, _ ->
                            arrayListOf("data")
                        }
                        execute<CommandSender> { sender, context, argument ->
                            giveAllCommandAsync(sender, context.argument(-3), context.argument(-2), context.argument(-1), argument)
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    // ni drop [??????ID] [??????] [?????????] [X??????] [Y??????] [Z??????] [??????????????????] [??????????????????] (????????????) > ?????????????????????NI??????
    val drop = subCommand {
        // ni drop [??????ID]
        dynamic(commit = "item") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                ItemManager.items.keys.toList()
            }
            execute<CommandSender> { sender, _, _ ->
                submit(async = true) {
                    help(sender)
                }
            }
            // ni drop [??????ID] [??????]
            dynamic(commit = "amount") {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    arrayListOf("amount")
                }
                execute<CommandSender> { sender, _, _ ->
                    submit(async = true) {
                        help(sender)
                    }
                }
                // ni drop [??????ID] [??????] [?????????]
                dynamic(commit = "world") {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        Bukkit.getWorlds().map { it.name }
                    }
                    execute<CommandSender> { sender, _, _ ->
                        submit(async = true) {
                            help(sender)
                        }
                    }
                    // ni drop [??????ID] [??????] [?????????] [X??????]
                    dynamic(commit = "x") {
                        suggestion<CommandSender>(uncheck = true) { _, _ ->
                            arrayListOf("x")
                        }
                        execute<CommandSender> { sender, _, _ ->
                            submit(async = true) {
                                help(sender)
                            }
                        }
                        // ni drop [??????ID] [??????] [?????????] [X??????] [Y??????]
                        dynamic(commit = "y") {
                            suggestion<CommandSender>(uncheck = true) { _, _ ->
                                arrayListOf("y")
                            }
                            execute<CommandSender> { sender, _, _ ->
                                submit(async = true) {
                                    help(sender)
                                }
                            }
                            // ni drop [??????ID] [??????] [?????????] [X??????] [Y??????] [Z??????]
                            dynamic(commit = "z") {
                                suggestion<CommandSender>(uncheck = true) { _, _ ->
                                    arrayListOf("z")
                                }
                                execute<CommandSender> { sender, _, _ ->
                                    submit(async = true) {
                                        help(sender)
                                    }
                                }
                                // ni drop [??????ID] [??????] [?????????] [X??????] [Y??????] [Z??????] [??????????????????]
                                dynamic(commit = "random") {
                                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                                        arrayListOf("true", "false")
                                    }
                                    execute<CommandSender> { sender, _, _ ->
                                        submit(async = true) {
                                            help(sender)
                                        }
                                    }
                                    // ni drop [??????ID] [??????] [?????????] [X??????] [Y??????] [Z??????] [??????????????????] [??????????????????]
                                    dynamic(commit = "data") {
                                        suggestion<CommandSender>(uncheck = true) { _, _ ->
                                            Bukkit.getOnlinePlayers().map { it.name }
                                        }
                                        execute<CommandSender> { sender, context, argument ->
                                            dropCommandAsync(sender, context.argument(-7), context.argument(-6), context.argument(-5), context.argument(-4), context.argument(-3), context.argument(-2), context.argument(-1), argument)
                                        }
                                        // ni drop [??????ID] [??????] [?????????] [X??????] [Y??????] [Z??????] [??????????????????] [??????????????????] (????????????)
                                        dynamic(optional = true, commit = "player") {
                                            suggestion<CommandSender>(uncheck = true) { _, _ ->
                                                arrayListOf("data")
                                            }
                                            execute<CommandSender> { sender, context, argument ->
                                                dropCommandAsync(sender, context.argument(-8), context.argument(-7), context.argument(-6), context.argument(-5), context.argument(-4), context.argument(-3), context.argument(-2), context.argument(-1), argument)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    // ni save [??????ID] (????????????) > ????????????????????????ID?????????????????????
    val save = subCommand {
        execute<Player> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ni save [??????ID]
        dynamic(commit = "id") {
            suggestion<Player>(uncheck = true) { _, _ ->
                arrayListOf("id")
            }
            execute<Player> { sender, _, argument ->
                submit(async = true) {
                    when (saveItem(sender.inventory.itemInMainHand, argument, "$argument.yml", false)) {
                        // ????????????
                        1 -> {
                            sender.sendMessage(config.getString("Messages.successSaveInfo")
                                ?.replace("{name}", sender.inventory.itemInMainHand.getName())
                                ?.replace("{itemID}", argument)
                                ?.replace("{path}", "$argument.yml"))
                        }
                        // ???????????????ID??????
                        0 -> sender.sendMessage(config.getString("Messages.existedKey")?.replace("{itemID}", argument))
                        // ?????????????????????
                        else -> sender.sendMessage(config.getString("Messages.airItem"))
                    }
                }
            }
            // ni save [??????ID] (????????????)
            dynamic(commit = "path") {
                suggestion<Player>(uncheck = true) { _, _ ->
                    ItemManager.files.map { it.path.replace("plugins${File.separator}NeigeItems${File.separator}Items${File.separator}", "") }
                }
                execute<Player> { sender, context, argument ->
                    submit(async = true) {
                        when (saveItem(sender.inventory.itemInMainHand, context.argument(-1), argument, false)) {
                            // ????????????
                            1 -> {
                                sender.sendMessage(config.getString("Messages.successSaveInfo")
                                    ?.replace("{name}", sender.inventory.itemInMainHand.getName())
                                    ?.replace("{itemID}", context.argument(-1))
                                    ?.replace("{path}", argument))
                            }
                            // ???????????????ID??????
                            0 -> sender.sendMessage(config.getString("Messages.existedKey")?.replace("{itemID}", context.argument(-1)))
                            // ?????????????????????
                            else -> sender.sendMessage(config.getString("Messages.airItem"))
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    // ni cover [??????ID] (????????????) > ????????????????????????ID?????????????????????
    val cover = subCommand {
        execute<Player> { sender, _, _ ->
            submit(async = true) {
                help(sender)
            }
        }
        // ni cover [??????ID]
        dynamic(commit = "id") {
            suggestion<Player>(uncheck = true) { _, _ ->
                arrayListOf("id")
            }
            execute<Player> { sender, _, argument ->
                submit(async = true) {
                    when (saveItem(sender.inventory.itemInMainHand, argument, "$argument.yml", true)) {
                        // ?????????????????????
                        2 -> sender.sendMessage(config.getString("Messages.airItem"))
                        // ????????????
                        else -> {
                            sender.sendMessage(config.getString("Messages.successSaveInfo")
                                ?.replace("{name}", sender.inventory.itemInMainHand.getName())
                                ?.replace("{itemID}", argument)
                                ?.replace("{path}", "$argument.yml"))
                        }
                    }
                }
            }
            // ni cover [??????ID] (????????????)
            dynamic(commit = "path") {
                suggestion<Player>(uncheck = true) { _, _ ->
                    ItemManager.files.map { it.path.replace("plugins${File.separator}NeigeItems${File.separator}Items${File.separator}", "") }
                }
                execute<Player> { sender, context, argument ->
                    submit(async = true) {
                        when (saveItem(sender.inventory.itemInMainHand, context.argument(-1), argument, true)) {
                            // ?????????????????????
                            2 -> sender.sendMessage(config.getString("Messages.airItem"))
                            // ????????????
                            else -> {
                                sender.sendMessage(config.getString("Messages.successSaveInfo")
                                    ?.replace("{name}", sender.inventory.itemInMainHand.getName())
                                    ?.replace("{itemID}", context.argument(-1))
                                    ?.replace("{path}", argument))
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val mm = subCommand {
        dynamic(commit = "action") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                arrayListOf("load", "cover", "loadAll", "get", "give", "giveAll")
            }
            execute<CommandSender> { sender, _, argument ->
                submit(async = true) {
                    mythicMobsHooker?.let {
                        when (argument) {
                            // ni mm loadAll
                            "loadAll" -> {
                                mythicMobsHooker!!.getItemIds().forEach { id ->
                                    mythicMobsHooker!!.getItemStackSync(id)?.let { itemStack ->
                                        when (saveItem(itemStack, id, config.getString("Main.MMItemsPath") ?: "MMItems.yml", false)) {
                                            // ????????????
                                            1 -> {
                                                sender.sendMessage(config.getString("Messages.successSaveInfo")
                                                    ?.replace("{name}", itemStack.getName())
                                                    ?.replace("{itemID}", id)
                                                    ?.replace("{path}", config.getString("Main.MMItemsPath") ?: "MMItems.yml"))
                                            }
                                            // ???????????????ID??????
                                            0 -> sender.sendMessage(config.getString("Messages.existedKey")?.replace("{itemID}", id))
                                        }
                                    }
                                }
                            }
                            else -> help(sender)
                        }
                    } ?: sender.sendMessage(config.getString("Messages.invalidPlugin")?.replace("{plugin}", "MythicMobs"))
                }
            }
            dynamic(commit = "action") {
                suggestion<CommandSender>(uncheck = true) { _, context ->
                    mythicMobsHooker?.let {
                        when (context.argument(-1)) {
                            // ni mm load [??????ID]
                            "load" -> mythicMobsHooker!!.getItemIds()
                            // ni mm cover [??????ID]
                            "cover" -> mythicMobsHooker!!.getItemIds()
                            // ni mm loadAll (????????????)
                            "loadAll" -> ItemManager.files.map { it.path.replace("plugins${File.separator}NeigeItems${File.separator}Items${File.separator}", "") }
                            // ni mm get [??????ID]
                            "get" -> mythicMobsHooker!!.getItemIds()
                            // ni mm give [??????ID]
                            "give" -> Bukkit.getOnlinePlayers().map { it.name }
                            // ni mm giveAll [??????ID]
                            "giveAll" -> mythicMobsHooker!!.getItemIds()
                            else -> arrayListOf()
                        }
                    } ?: arrayListOf()
                }
                execute<CommandSender> { sender, context, argument ->
                    submit(async = true) {
                        mythicMobsHooker?.let {
                            when (context.argument(-1)) {
                                // ni mm load [??????ID]
                                "load" -> {
                                    mythicMobsHooker!!.getItemStackSync(argument)?.let { itemStack ->
                                        when (saveItem(itemStack, argument, "$argument.yml", false)) {
                                            // ????????????
                                            1 -> {
                                                sender.sendMessage(config.getString("Messages.successSaveInfo")
                                                    ?.replace("{name}", itemStack.getName())
                                                    ?.replace("{itemID}", argument)
                                                    ?.replace("{path}", "$argument.yml"))
                                            }
                                            // ???????????????ID??????
                                            0 -> sender.sendMessage(config.getString("Messages.existedKey")?.replace("{itemID}", argument))
                                        }
                                        // ????????????
                                    } ?: let {
                                        sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", argument))
                                    }
                                }
                                // ni mm cover [??????ID]
                                "cover" -> {
                                    mythicMobsHooker!!.getItemStackSync(argument)?.let { itemStack ->
                                        if (saveItem(itemStack, argument, "$argument.yml", true) != 2) {
                                            // ????????????
                                            sender.sendMessage(config.getString("Messages.successSaveInfo")
                                                ?.replace("{name}", itemStack.getName())
                                                ?.replace("{itemID}", argument)
                                                ?.replace("{path}", "$argument.yml"))
                                        }
                                        // ????????????
                                    } ?: let {
                                        sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", argument))
                                    }
                                }
                                // ni mm loadAll (????????????)
                                "loadAll" -> {
                                    mythicMobsHooker!!.getItemIds().forEach { id ->
                                        mythicMobsHooker!!.getItemStackSync(id)?.let { itemStack ->
                                            when (saveItem(itemStack, id, argument, false)) {
                                                // ????????????
                                                1 -> {
                                                    sender.sendMessage(config.getString("Messages.successSaveInfo")
                                                        ?.replace("{name}", itemStack.getName())
                                                        ?.replace("{itemID}", id)
                                                        ?.replace("{path}", argument))
                                                }
                                                // ???????????????ID??????
                                                0 -> sender.sendMessage(config.getString("Messages.existedKey")?.replace("{itemID}", id))
                                            }
                                        }
                                    }
                                }
                                // ni mm get [??????ID]
                                "get" -> {
                                    if (sender is Player) {
                                        giveAddonCommand( sender, sender, argument, mythicMobsHooker!!.getItemStackSync(argument), 1)
                                    } else {
                                        config.getString("Messages.onlyPlayer")?.let { sender.sendMessage(it) }
                                    }
                                }
                                // ni mm giveAll [??????ID]
                                "giveAll" -> {
                                    Bukkit.getOnlinePlayers().forEach { player ->
                                        giveAddonCommand( sender, player, argument, mythicMobsHooker!!.getItemStackSync(argument), 1)
                                    }
                                }
                                else -> help(sender)
                            }
                        } ?: sender.sendMessage(config.getString("Messages.invalidPlugin")?.replace("{plugin}", "MythicMobs"))
                    }
                }
                dynamic(commit = "action") {
                    suggestion<CommandSender>(uncheck = true) { _, context ->
                        mythicMobsHooker?.let {
                            when (context.argument(-2)) {
                                // ni mm load [??????ID] (????????????)
                                "load" -> ItemManager.files.map { it.path.replace("plugins${File.separator}NeigeItems${File.separator}Items${File.separator}", "") }
                                // ni mm cover [??????ID] (????????????)
                                "cover" -> ItemManager.files.map { it.path.replace("plugins${File.separator}NeigeItems${File.separator}Items${File.separator}", "") }
                                // ni mm get [??????ID] (??????)
                                "get" -> arrayListOf("amount")
                                // ni mm give [??????ID] [??????ID]
                                "give" -> mythicMobsHooker!!.getItemIds()
                                // ni mm giveAll [??????ID] (??????)
                                "giveAll" -> arrayListOf("amount")
                                else -> arrayListOf()
                            }
                        } ?: arrayListOf()
                    }
                    execute<CommandSender> { sender, context, argument ->
                        submit(async = true) {
                            mythicMobsHooker?.let {
                                when (context.argument(-2)) {
                                    // ni mm load [??????ID] (????????????)
                                    "load" -> {
                                        mythicMobsHooker!!.getItemStackSync(context.argument(-1))?.let { itemStack ->
                                            when (saveItem(itemStack, context.argument(-1), argument, false)) {
                                                // ????????????
                                                1 -> {
                                                    sender.sendMessage(config.getString("Messages.successSaveInfo")
                                                        ?.replace("{name}", itemStack.getName())
                                                        ?.replace("{itemID}", context.argument(-1))
                                                        ?.replace("{path}", argument))
                                                }
                                                // ???????????????ID??????
                                                0 -> sender.sendMessage(config.getString("Messages.existedKey")?.replace("{itemID}", context.argument(-1)))
                                            }
                                            // ????????????
                                        } ?: let {
                                            sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", context.argument(-1)))
                                        }
                                    }
                                    // ni mm cover [??????ID] (????????????)
                                    "cover" -> {
                                        mythicMobsHooker!!.getItemStackSync(context.argument(-1))?.let { itemStack ->
                                            if (saveItem(itemStack, context.argument(-1), argument, true) != 2) {
                                                // ????????????
                                                sender.sendMessage(config.getString("Messages.successSaveInfo")
                                                    ?.replace("{name}", itemStack.getName())
                                                    ?.replace("{itemID}", context.argument(-1))
                                                    ?.replace("{path}", argument))
                                            }
                                            // ????????????
                                        } ?: let {
                                            sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", context.argument(-1)))
                                        }
                                    }
                                    // ni mm get [??????ID] (??????)
                                    "get" -> {
                                        if (sender is Player) {
                                            giveAddonCommand( sender, sender, context.argument(-1), mythicMobsHooker!!.getItemStackSync(context.argument(-1)), argument.toIntOrNull())
                                        } else {
                                            config.getString("Messages.onlyPlayer")?.let { sender.sendMessage(it) }
                                        }
                                    }
                                    // ni mm give [??????ID] [??????ID]
                                    "give" -> {
                                        giveAddonCommand( sender, Bukkit.getPlayerExact(context.argument(-1)), argument, mythicMobsHooker!!.getItemStackSync(argument), 1)
                                    }
                                    // ni mm giveAll [??????ID] (??????)
                                    "giveAll" -> {
                                        Bukkit.getOnlinePlayers().forEach { player ->
                                            giveAddonCommand( sender, player, context.argument(-1), mythicMobsHooker!!.getItemStackSync(context.argument(-1)), argument.toIntOrNull())
                                        }
                                    }
                                    else -> help(sender)
                                }
                            } ?: sender.sendMessage(config.getString("Messages.invalidPlugin")?.replace("{plugin}", "MythicMobs"))
                        }
                    }
                    dynamic(commit = "action") {
                        suggestion<CommandSender>(uncheck = true) { _, context ->
                            mythicMobsHooker?.let {
                                when (context.argument(-3)) {
                                    // ni mm give [??????ID] [??????ID] (??????)
                                    "give" -> mythicMobsHooker!!.getItemIds()
                                    else -> arrayListOf()
                                }
                            } ?: arrayListOf()
                        }
                        execute<CommandSender> { sender, context, argument ->
                            submit(async = true) {
                                mythicMobsHooker?.let {
                                    when (context.argument(-3)) {
                                        // ni mm give [??????ID] [??????ID] (??????)
                                        "give" -> {
                                            giveAddonCommand( sender, Bukkit.getPlayerExact(context.argument(-2)), context.argument(-1), mythicMobsHooker!!.getItemStackSync(context.argument(-1)), argument.toIntOrNull())
                                        }
                                        else -> help(sender)
                                    }
                                } ?: sender.sendMessage(config.getString("Messages.invalidPlugin")?.replace("{plugin}", "MythicMobs"))
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            reloadCommand(sender)
        }
    }

    @CommandBody
    val help = subCommand {
        execute<CommandSender> { sender, _, _ ->
            help(sender)
        }
    }
    private fun listCommandAsync (sender: CommandSender, page: Int) {
        submit (async = true) {
            listCommand(sender, page)
        }
    }

    private fun listCommand (
        // ???????????????, ????????????????????????
        sender: CommandSender,
        // ??????
        page: Int
    ) {
        val pageAmount = ceil(ItemManager.itemAmount.toDouble()/config.getDouble("ItemList.ItemAmount")).toInt()
        val realPage = page.coerceAtMost(pageAmount).coerceAtLeast(1)
        // ????????????
        config.getString("ItemList.Prefix")?.let { sender.sendMessage(it) }
        // ????????????????????????
        val listMessage = TellrawJson()
        // ??????????????????
        val prevItemAmount = ((realPage-1)*config.getInt("ItemList.ItemAmount"))+1
        // ??????????????????
        for (index in (prevItemAmount until prevItemAmount + config.getInt("ItemList.ItemAmount"))) {
            if (index == ItemManager.itemIds.size + 1) break
            val id = ItemManager.itemIds[index-1]
            // ?????????????????????
            var listItemMessage = (config.getString("ItemList.ItemFormat") ?: "")
                .replace("{index}", index.toString())
                .replace("{ID}", id)
            // ?????????????????????
            if (sender is Player) {
                getItemStack(id, sender)?.let { itemStack ->
                    val listItemMessageList = listItemMessage.split("{name}")
                    val listItemRaw = TellrawJson()
                    for ((i, it) in listItemMessageList.withIndex()) {
                        listItemRaw.append(
                            TellrawJson()
                                .append(it)
                                .runCommand("/ni get $id")
                                .hoverText(config.getString("Messages.clickGiveMessage")?:"")
                        )
                        if (i+1 != listItemMessageList.size) {
                            listItemRaw.append(
                                TellrawJson()
                                    .append(itemStack.getName())
                                    .hoverItem(itemStack)
                                    .runCommand("/ni get $id")
                            )
                        }
                    }
                    listItemRaw.sendTo(bukkitAdapter.adaptCommandSender(sender))
                }
            } else {
                // ?????????????????????????????????????????????????????????????????????
                // ???????????????js??????????????????????????????, ?????????????????????
                try {
                    getItemStack(id)?.let { itemStack ->
                        sender.sendMessage(listItemMessage.replace("{name}", itemStack.getName()))
                    }
                } catch (error: Throwable) {
                    val itemKeySection = ItemManager.getOriginConfig(id)
                    val itemName = when {
                        itemKeySection?.contains("name") == true -> itemKeySection.getString("name")
                        else -> Material.matchMaterial((itemKeySection?.getString("material")?:"").uppercase(Locale.getDefault()))
                            ?.let { ItemStack(it).getName() }
                    }
                    listItemMessage = itemName?.let { listItemMessage.replace("{name}", it) }.toString()
                    sender.sendMessage(listItemMessage)
                }
            }
        }
        val prevRaw = TellrawJson()
            .append(config.getString("ItemList.Prev")?:"")
        if (realPage != 1) {
            prevRaw
                .hoverText((config.getString("ItemList.Prev")?:"") + ": " + (realPage-1).toString())
                .runCommand("/ni list ${realPage-1}")
        }
        val nextRaw = TellrawJson()
            .append(config.getString("ItemList.Next")?:"")
        if (realPage != pageAmount) {
            nextRaw.hoverText((config.getString("ItemList.Next")?:"") + ": " + (realPage+1))
            nextRaw.runCommand("/ni list ${realPage+1}")
        }
        var listSuffixMessage = (config.getString("ItemList.Suffix")?:"")
            .replace("{current}", realPage.toString())
            .replace("{total}", pageAmount.toString())
        if (sender is Player) {
            listSuffixMessage = listSuffixMessage
                .replace("{prev}", "!@#$%{prev}!@#$%")
                .replace("{next}", "!@#$%{next}!@#$%")
            val listSuffixMessageList = listSuffixMessage.split("!@#$%")
            listSuffixMessageList.forEach { value ->
                when (value) {
                    "{prev}" -> listMessage.append(prevRaw)
                    "{next}" -> listMessage.append(nextRaw)
                    else -> listMessage.append(value)
                }
            }
            // ?????????????????????
            listMessage.sendTo(bukkitAdapter.adaptCommandSender(sender))
        } else {
            sender.sendMessage(listSuffixMessage
                .replace("{prev}", config.getString("ItemList.Prev")?:"")
                .replace("{next}", config.getString("ItemList.Next")?:""))
        }
    }

    private fun giveCommand(
        // ???????????????, ????????????????????????
        sender: CommandSender,
        // ???????????????
        player: Player?,
        // ???????????????ID
        id: String,
        // ????????????
        amount: String?,
        // ??????????????????
        random: String?,
        // ????????????
        data: String?
    ) {
        giveCommand(sender, player, id, amount?.toIntOrNull(), random, data)
    }

    private fun giveCommandAsync(
        sender: CommandSender,
        player: Player?,
        id: String,
        amount: String? = null,
        random: String? = null,
        data: String? = null
    ) {
        submit(async = true) {
            giveCommand(sender, player, id, amount, random, data)
        }
    }

    private fun giveAllCommandAsync(
        sender: CommandSender,
        id: String,
        amount: String? = null,
        random: String? = null,
        data: String? = null
    ) {
        submit(async = true) {
            Bukkit.getOnlinePlayers().forEach { player ->
                giveCommand(sender, player, id, amount, random, data)
            }
        }
    }

    private fun giveCommand(
        sender: CommandSender,
        player: Player?,
        id: String,
        amount: Int?,
        random: String?,
        data: String?
    ) {
        player?.let {
            when (random) {
                "false", "0" -> {
                    // ????????????
                    amount?.let {
                        // ?????????
                        getItemStack(id, player, data)?.let { itemStack ->
                            bukkitScheduler.callSyncMethod(plugin) {
                                player.giveItems(itemStack, amount.coerceAtLeast(1))
                            }
                            sender.sendMessage(config.getString("Messages.successInfo")
                                ?.replace("{player}", player.name)
                                ?.replace("{amount}", amount.toString())
                                ?.replace("{name}", itemStack.getName()))
                            player.sendMessage(config.getString("Messages.givenInfo")
                                ?.replace("{amount}", amount.toString())
                                ?.replace("{name}", itemStack.getName()))
                            // ????????????ID
                        } ?: let {
                            sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", id))
                        }
                        // ????????????
                    } ?: let {
                        sender.sendMessage(config.getString("Messages.invalidAmount"))
                    }
                }
                else -> {
                    // ????????????
                    amount?.let {
                        val dropData = HashMap<String, Int>()
                        // ?????????
                        repeat(amount.coerceAtLeast(1)) {
                            getItemStack(id, player, data)?.let { itemStack ->
                                bukkitScheduler.callSyncMethod(plugin) {
                                    player.giveItem(itemStack)
                                }
                                dropData[itemStack.getName()] = dropData[itemStack.getName()]?.let { it + 1 } ?: let { 1 }
                                // ????????????ID
                            } ?: let {
                                sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", id))
                                return@repeat
                            }
                        }
                        for ((name, amt) in dropData) {
                            sender.sendMessage(config.getString("Messages.successInfo")
                                ?.replace("{player}", player.name)
                                ?.replace("{amount}", amt.toString())
                                ?.replace("{name}", name))
                            player.sendMessage(config.getString("Messages.givenInfo")
                                ?.replace("{amount}", amt.toString())
                                ?.replace("{name}", name))
                        }
                        // ????????????
                    } ?: let {
                        sender.sendMessage(config.getString("Messages.invalidAmount"))
                    }
                }
            }
            // ????????????
        } ?: let {
            sender.sendMessage(config.getString("Messages.invalidPlayer"))
        }
    }

    private fun giveAddonCommand(
        sender: CommandSender,
        player: Player?,
        id: String,
        itemStack: ItemStack?,
        amount: Int?
    ) {
        submit (async = true) {
            player?.let {
                // ????????????
                amount?.let {
                    // ?????????
                    itemStack?.let {
                        bukkitScheduler.callSyncMethod(plugin) {
                            player.giveItems(itemStack, amount.coerceAtLeast(1))
                        }
                        sender.sendMessage(config.getString("Messages.successInfo")
                            ?.replace("{player}", player.name)
                            ?.replace("{amount}", amount.toString())
                            ?.replace("{name}", itemStack.getName()))
                        player.sendMessage(config.getString("Messages.givenInfo")
                            ?.replace("{amount}", amount.toString())
                            ?.replace("{name}", itemStack.getName()))
                        // ????????????ID
                    } ?: let {
                        sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", id))
                    }
                    // ????????????
                } ?: let {
                    sender.sendMessage(config.getString("Messages.invalidAmount"))
                }
                // ????????????
            } ?: let {
                sender.sendMessage(config.getString("Messages.invalidPlayer"))
            }
        }
    }

    private fun dropCommand(
        // ???????????????, ????????????????????????
        sender: CommandSender,
        // ???????????????ID
        id: String,
        // ????????????
        amount: String,
        // ???????????????
        worldName: String,
        // ????????????x??????
        xString: String,
        // ????????????y??????
        yString: String,
        // ????????????z??????
        zString: String,
        // ??????????????????
        random: String,
        // ??????????????????, ??????????????????
        parser: String,
        // ????????????
        data: String?
    ) {
        Bukkit.getWorld(worldName)?.let { world ->
            val x = xString.toDoubleOrNull()
            val y = yString.toDoubleOrNull()
            val z = zString.toDoubleOrNull()
            if (x != null && y != null && z != null) {
                dropCommand(sender, id, amount.toIntOrNull(), Location(world, x, y, z), random, Bukkit.getPlayerExact(parser), data)
            } else {
                sender.sendMessage(config.getString("Messages.invalidLocation"))
            }
        } ?: let {
            sender.sendMessage(config.getString("Messages.invalidWorld"))
        }
    }

    private fun dropCommandAsync(
        sender: CommandSender,
        id: String,
        amount: String,
        worldName: String,
        xString: String,
        yString: String,
        zString: String,
        random: String,
        parser: String,
        data: String? = null
    ) {
        submit(async = true) {
            dropCommand(sender, id, amount, worldName, xString, yString, zString, random, parser, data)
        }
    }

    private fun dropCommand(
        sender: CommandSender,
        id: String,
        amount: Int?,
        location: Location?,
        random: String,
        parser: Player?,
        data: String?
    ) {
        parser?.let {
            when (random) {
                "false", "0" -> {
                    // ????????????
                    amount?.let {
                        // ?????????
                        getItemStack(id, parser, data)?.let { itemStack ->
                            location?.dropNiItems(itemStack, amount.coerceAtLeast(1))
                            sender.sendMessage(config.getString("Messages.dropSuccessInfo")
                                ?.replace("{world}", location?.world?.name ?: "")
                                ?.replace("{x}", location?.x.toString())
                                ?.replace("{y}", location?.y.toString())
                                ?.replace("{z}", location?.z.toString())
                                ?.replace("{amount}", amount.toString())
                                ?.replace("{name}", itemStack.getName()))
                            // ????????????ID
                        } ?: let {
                            sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", id))
                        }
                        // ????????????
                    } ?: let {
                        sender.sendMessage(config.getString("Messages.invalidAmount"))
                    }
                }
                else -> {
                    // ????????????
                    amount?.let {
                        val dropData = HashMap<String, Int>()
                        // ?????????
                        repeat(amount.coerceAtLeast(1)) {
                            getItemStack(id, parser, data)?.let { itemStack ->
                                location?.dropNiItem(itemStack)
                                dropData[itemStack.getName()] = dropData[itemStack.getName()]?.let { it + 1 } ?: let { 1 }
                                // ????????????ID
                            } ?: let {
                                sender.sendMessage(config.getString("Messages.unknownItem")?.replace("{itemID}", id))
                                return@repeat
                            }
                        }
                        for((name, amt) in dropData) {
                            sender.sendMessage(config.getString("Messages.dropSuccessInfo")
                                ?.replace("{world}", location?.world?.name ?: "")
                                ?.replace("{x}", location?.x.toString())
                                ?.replace("{y}", location?.y.toString())
                                ?.replace("{z}", location?.z.toString())
                                ?.replace("{amount}", amt.toString())
                                ?.replace("{name}", name))
                        }
                        // ????????????
                    } ?: let {
                        sender.sendMessage(config.getString("Messages.invalidAmount"))
                    }
                }
            }
            // ??????????????????
        } ?: let {
            sender.sendMessage(config.getString("Messages.invalidParser"))
        }
    }

    private fun reloadCommand(sender: CommandSender) {
        submit(async = true) {
            ConfigManager.reload()
            ItemManager.reload()
            ScriptManager.reload()
            SectionManager.reload()
            ActionManager.reload()
            sender.sendMessage(config.getString("Messages.reloadedMessage"))
        }
    }

    private fun help(sender: CommandSender) {
        config.getStringList("Messages.helpMessages").forEach {
            sender.sendMessage(it)
        }
    }

    private fun help(sender: ProxyCommandSender) {
        config.getStringList("Messages.helpMessages").forEach {
            sender.sendMessage(it)
        }
    }
}