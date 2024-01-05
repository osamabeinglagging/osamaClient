package dev.liquid.osamaclient.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.DualOption
import cc.polyfrost.oneconfig.config.annotations.HUD
import cc.polyfrost.oneconfig.config.annotations.Info
import cc.polyfrost.oneconfig.config.annotations.KeyBind
import cc.polyfrost.oneconfig.config.annotations.Number
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.InfoType
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.OptionSize
import cc.polyfrost.oneconfig.libs.universal.UKeyboard
import dev.liquid.osamaclient.OsamaClient
import dev.liquid.osamaclient.config.hud.ForagingMacroHUD
import dev.liquid.osamaclient.util.macroHandler
import dev.liquid.osamaclient.util.osamaClient

class OsamaClientConfig : Config(Mod("OsamaClient", ModType.UTIL_QOL), "osamaclient.json") {

  // ==============================
  //            General
  // ==============================

  @KeyBind(
    name = "Macro Toggle",
    category = "General",
    subcategory = "Macros"
  )
  val macroToggleKeyBind = OneKeyBind(UKeyboard.KEY_GRAVE)

  @Dropdown(
    name = "Macro",
    category = "General",
    subcategory = "Macros",
    options = ["ForagingMacro"]
  )
  val activeMacro = 0

  @Switch(
    name = "Ungrab Mouse (Does not work on MacOS)",
    category = "General",
    subcategory = "Macros"
  )
  val ungrabMouse = false

  @Switch(
    name = "Debug Mode",
    category = "General",
    subcategory = "Debug"
  )
  var osamaClientDebugMode = false

  // ==============================
  //        Foraging Macro
  // ==============================

  @Info(
    text = "Must Use The Layout on Github Page.",
    size = OptionSize.SINGLE,
    type = InfoType.WARNING,
    category = "Foraging Macro"
  )
  private var foragingMacroLayoutWarning = ""

  @Info(
    text = "You need to have monkey pet in pets menu for this to work. AND, pet must spawn during the macro is enabled.",
    size = OptionSize.SINGLE,
    type = InfoType.INFO,
    category = "Foraging Macro"
  )
  private var foragingMacroPetWarning = ""

  @Dropdown(
    name = "Sapling Type",
    size = OptionSize.DUAL,
    category = "Foraging Macro",
    options = ["Jungle Sapling", "Dark Oak Sapling", "Spruce Sapling", "Acacia Sapling"]
  )
  var foragingMacroSaplingType = 0

  @Switch(
    name = "Rod Swap (Must Have Leg Monkey)",
    category = "Foraging Macro"
  )
  var foragingMacroRodSwap = false

  @Switch(
    name = "Monkey Equipped",
    description = "In case you want to forage with monkey pet",
    category = "Foraging Macro",
  )
  var foragingMacroDefaultMonkey = false

  @Info(
    text = "You must have Abiphone (With Builder and Farm Merchant Contacts) in your inventory for this to work.",
    type = InfoType.INFO,
    category = "Foraging Macro",
    subcategory = "AutoRefill"
  )
  private var foragingMacroAutoRefillWarning = ""

  @Switch(
    name = "AutoRefill Chests",
    category = "Foraging Macro",
    subcategory = "AutoRefill"
  )
  var foragingMacroAutoRefill = false


  // ==============================
  //          Mini Macros
  // ==============================
  // Auto Enchanting

  @Switch(
    name = "Auto Experiment",
    category = "Mini Macros",
    subcategory = "Auto Experiment"
  )
  var autoExperiment = false

  @Slider(
    name = "Delay Between Clicks",
    category = "Mini Macros",
    subcategory = "Auto Experiment",
    min = 100f, max = 1000f, step = 50
  )
  var autoExperimentClickDelay = 400

  @Number(
    name = "Max Chronomatron Rounds",
    category = "Mini Macros",
    subcategory = "Auto Experiment",
    min = 1f, max = 20f
  )
  var autoExperimentChronomatronMax = 12

  @Number(
    name = "Max Ultrasequencer Rounds",
    category = "Mini Macros",
    subcategory = "Auto Experiment",
    min = 1f, max = 20f
  )
  var autoExperimentSequencerMax = 12

  // ==============================
  //              ESP
  // ==============================

  @Switch(
    name = "Particle ESP",
    category = "ESP",
    subcategory = "Particle"
  )
  var particleEsp = false

  @Dropdown(
    name = "Particle Type",
    category = "ESP",
    subcategory = "Particle",
    options = ["Explode", "Large Explode", "Huge Explosion", "Fireworks Spark", "Bubble", "Splash", "Wake", "Suspended", "Depth Suspend", "Crit", "Magic Crit", "Smoke", "Large Smoke", "Spell", "Instant Spell", "Mob Spell", "Mob Spell Ambient", "Witch Magic", "Drip Water", "Drip Lava", "Angry Villager", "Happy Villager", "Town Aura", "Note", "Portal", "Enchantment Table", "Flame", "Lava", "Footstep", "Cloud", "Red Dust", "Snowball Poof", "Snow Shovel", "Slime", "Heart", "Barrier", "Icon Crack", "Block Crack", "Block Dust", "Droplet", "Take", "Mob Appearance"]
  )
  var particleEspParticleID = 0

  @Slider(
    name = "ESP Size (%)",
    category = "ESP",
    subcategory = "Particle",
    min = 1f, max = 50f, step = 5
  )
  var particleEspParticleSize = 10

  @Slider(
    name = "ESP Time (Frame)",
    category = "ESP",
    subcategory = "Particle",
    min = 1f, max = 120f, step = 1
  )
  var particleEspParticleTime = 10

  @Color(
    name = "ESP Color",
    allowAlpha = true,
    category = "ESP",
    subcategory = "Particle"
  )
  var particleEspEspColor = OneColor(0, 255, 255)

  // ==========================
  //        Features
  // ==========================

  // AutoSell
  @Info(
    text = "Auto Sell Requires Cookie. Make sure you have bone meal removed from instasell.",
    size = OptionSize.DUAL,
    category = "Features",
    subcategory = "AutoSell",
    type = InfoType.INFO
  )
  private var _featureAutoSellCookieWarning = ""

  @Switch(
    name = "AutoSell",
    category = "Features",
    subcategory = "AutoSell"
  )
  var featureAutoSellEnable = false

  @DualOption(
    name = "SellType",
    category = "Features",
    subcategory = "AutoSell",
    left = "Inventory",
    right = "Inventory And Sack"
  )
  var featureAutoSellType = false;

  @Slider(
    name = "Inventory Full Percentage",
    category = "Features",
    subcategory = "AutoSell",
    min = 10f, max = 100f
  )
  var featureAutoSellInvFullPercentage = 60

  // ==========================
  //            HUD
  // ==========================
  @HUD(
    name = "Foraging Macro HUD",
    category = "HUD",
    subcategory = "Foraging Macro"
  )
  var foragingMacroHud = osamaClient.hud

  init {
    initialize()
    this.registerKeyBind(macroToggleKeyBind) { macroHandler.toggle() }
  }
}
