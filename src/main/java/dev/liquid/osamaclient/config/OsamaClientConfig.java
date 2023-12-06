package dev.liquid.osamaclient.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class OsamaClientConfig extends Config {

  // ==============================
  //              ESP
  // ==============================
  @Switch(
      name = "Particle ESP",
      category = "ESP",
      subcategory = "Particle"
  )
  public static boolean particleEsp = false;

  @Dropdown(
      name = "Particle Type",
      category = "ESP",
      subcategory = "Particle",
      options = {
          "Explode",
          "Large Explode",
          "Huge Explosion",
          "Fireworks Spark",
          "Bubble",
          "Splash",
          "Wake",
          "Suspended",
          "Depth Suspend",
          "Crit",
          "Magic Crit",
          "Smoke",
          "Large Smoke",
          "Spell",
          "Instant Spell",
          "Mob Spell",
          "Mob Spell Ambient",
          "Witch Magic",
          "Drip Water",
          "Drip Lava",
          "Angry Villager",
          "Happy Villager",
          "Town Aura",
          "Note",
          "Portal",
          "Enchantment Table",
          "Flame",
          "Lava",
          "Footstep",
          "Cloud",
          "Red Dust",
          "Snowball Poof",
          "Snow Shovel",
          "Slime",
          "Heart",
          "Barrier",
          "Icon Crack",
          "Block Crack",
          "Block Dust",
          "Droplet",
          "Take",
          "Mob Appearance"
      }
  )
  public static int particleEspParticleID = 0;

  @Slider(
      name = "ESP Size (%)",
      category = "ESP",
      subcategory = "Particle",
      min = 1f, max = 50f, step = 5
  )
  public static int particleEspParticleSize = 10;

  @Slider(
      name = "ESP Time (Frame)",
      category = "ESP",
      subcategory = "Particle",
      min = 1f, max = 120f, step = 1
  )
  public static int particleEspParticleTime = 10;

  @Color(
      name = "ESP Color",
      allowAlpha = true,
      category = "ESP",
      subcategory = "Particle"
  )
  public static OneColor particleEspEspColor = new OneColor(0, 255, 255);

  public OsamaClientConfig() {
    super(new Mod("OsamaClient", ModType.UTIL_QOL), "osamaclient.json");
    initialize();
  }
}
