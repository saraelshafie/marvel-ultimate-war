package model.effects;

import model.world.Champion;

public class Embrace extends Effect {

	public Embrace(int duration) {
		super("Embrace", duration, EffectType.BUFF);
	}

	@Override
	public void apply(Champion c) {
		c.setCurrentHP((int) (c.getMaxHP() * 0.2) + c.getCurrentHP());
		c.setMana((int) (c.getMana() * 1.2));
		c.setSpeed((int) (c.getSpeed() * 1.2));
		c.setAttackDamage((int) (c.getAttackDamage() * 1.2));

		c.setMaxHp((int)(c.getMaxHP() * 1.2));
		c.setMaxMana((int)(c.getMaxMana() * 1.2));
		c.setMaxSpeed((int)(c.getMaxSpeed() * 1.2));
		c.setMaxAttackDamage((int)(c.getMaxAttackDamage() * 1.2));

	}

	@Override
	public void remove(Champion c) {
		
		c.setSpeed((int) (c.getSpeed() / 1.2));
		c.setAttackDamage((int) (c.getAttackDamage() / 1.2));
		c.setMaxSpeed((int)(c.getMaxSpeed() / 1.2));
		c.setMaxAttackDamage((int)(c.getMaxAttackDamage() / 1.2));

	}

}
