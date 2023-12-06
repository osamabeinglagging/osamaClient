package dev.liquid.osamaclient.feature;

public interface IFeature {
  public void disable();
  public String getFeatureName();
  public boolean canEnable();
  public boolean isPassiveFeature();
}
