package cr0s.javara.entity;

/**
 * Determines entity can deploy.
 * @author Cr0s
 */
public interface IDeployable {
    public boolean canDeploy();
    public void deploy();
}
