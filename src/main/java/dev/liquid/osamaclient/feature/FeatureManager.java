package dev.liquid.osamaclient.feature;

import dev.liquid.osamaclient.feature.impl.esp.ParticleESP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureManager {
  public static FeatureManager instance;
  private final ArrayList<IFeature> features = new ArrayList<>();

  public static FeatureManager getInstance() {
    if (instance == null) {
      instance = new FeatureManager();
    }
    return instance;
  }

  public ArrayList<IFeature> loadFeatures() {
    List<IFeature> features = Arrays.asList(
        ParticleESP.getInstance()
    );
    this.features.addAll(features);
    return this.features;
  }

  public void disableFeatures(boolean disablePassiveFeatures){
    this.features.forEach(feature -> {
      if (!feature.isPassiveFeature() || (feature.isPassiveFeature() && disablePassiveFeatures)) {
        feature.disable();
      }
    });
  }
}
