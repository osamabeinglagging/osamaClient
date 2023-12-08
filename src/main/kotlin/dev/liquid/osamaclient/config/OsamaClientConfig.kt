package dev.liquid.osamaclient.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Color
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType

class OsamaClientConfig : Config(Mod("OsamaClient", ModType.UTIL_QOL), "osamaclient.json") {

  // ==============================
  //            General
  // ==============================

  @Switch(
    name = "Debug Mode",
    category = "General",
    subcategory = "Debug"
  )
  var osamaClientDebugMode = false

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

  init {
    initialize()
  }
}
