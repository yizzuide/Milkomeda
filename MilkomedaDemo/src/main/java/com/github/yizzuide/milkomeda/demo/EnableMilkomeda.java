package com.github.yizzuide.milkomeda.demo;

import com.github.yizzuide.milkomeda.atom.EnableAtom;
import com.github.yizzuide.milkomeda.comet.core.EnableComet;
import com.github.yizzuide.milkomeda.crust.EnableCrust;
import com.github.yizzuide.milkomeda.echo.EnableEcho;
import com.github.yizzuide.milkomeda.fusion.EnableFusion;
import com.github.yizzuide.milkomeda.halo.EnableHalo;
import com.github.yizzuide.milkomeda.hydrogen.core.EnableHydrogen;
import com.github.yizzuide.milkomeda.ice.EnableIce;
import com.github.yizzuide.milkomeda.jupiter.EnableJupiter;
import com.github.yizzuide.milkomeda.light.EnableLight;
import com.github.yizzuide.milkomeda.metal.EnableMetal;
import com.github.yizzuide.milkomeda.moon.EnableMoon;
import com.github.yizzuide.milkomeda.neutron.EnableNeutron;
import com.github.yizzuide.milkomeda.orbit.EnableOrbit;
import com.github.yizzuide.milkomeda.particle.EnableParticle;
import com.github.yizzuide.milkomeda.pillar.EnablePillar;
import com.github.yizzuide.milkomeda.pulsar.EnablePulsar;
import com.github.yizzuide.milkomeda.quark.EnableQuark;
import com.github.yizzuide.milkomeda.sundial.EnableSundial;
import com.github.yizzuide.milkomeda.wormhole.EnableWormhole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableMilkomeda
 * Milkomeda里的模块开启集合
 *
 * @author yizzuide
 * <br>
 * Create at 2019/12/13 01:03
 */
@EnableQuark
@EnableOrbit
@EnablePillar
@EnableMetal
@EnableJupiter
@EnableSundial
@EnableWormhole
@EnableAtom
@EnableHydrogen
@EnableMoon
@EnableHalo
@EnableNeutron
@EnableIce
@EnableCrust
@EnableEcho
@EnableLight
@EnableParticle
@EnablePulsar
@EnableFusion
@EnableComet
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableMilkomeda {
}
