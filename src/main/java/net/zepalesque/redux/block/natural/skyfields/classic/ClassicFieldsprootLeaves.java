package net.zepalesque.redux.block.natural.skyfields.classic;

import com.aetherteam.aether.block.natural.AetherDoubleDropsLeaves;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.common.util.Lazy;
import net.zepalesque.redux.block.natural.skyfields.FieldsprootLeafBlock;
import net.zepalesque.redux.block.util.state.ReduxStates;
import net.zepalesque.redux.client.particle.ReduxParticleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClassicFieldsprootLeaves extends AetherDoubleDropsLeaves {

    public static final Lazy<List<Supplier<? extends ParticleOptions>>> SPECTRAL = Lazy.of(() -> {
        List<Supplier<? extends ParticleOptions>> list = new ArrayList<>();
        list.set(0, ReduxParticleTypes.FIELDSPROUT_PETALS_0);
        list.set(1, ReduxParticleTypes.FIELDSPROUT_PETALS_1);
        list.set(2, ReduxParticleTypes.FIELDSPROUT_PETALS_2);
        return list;
    });
    public static final Lazy<List<Supplier<? extends ParticleOptions>>> PRISMATIC = Lazy.of(() -> {
        List<Supplier<? extends ParticleOptions>> list = new ArrayList<>();
        list.set(0, ReduxParticleTypes.FIELDSPROUT_PETALS_4);
        list.set(1, ReduxParticleTypes.FIELDSPROUT_PETALS_5);
        list.set(2, ReduxParticleTypes.FIELDSPROUT_PETALS_6);
        return list;
    });
    private final Supplier<List<Supplier<? extends ParticleOptions>>> particles;

    public ClassicFieldsprootLeaves(Supplier<List<Supplier<? extends ParticleOptions>>> particles, Properties properties) {
        super(properties);
        this.particles = particles;
        this.registerDefaultState(this.defaultBlockState().setValue(ReduxStates.PRISMATICNESS_DECREASED, 0));
    }

    public ParticleOptions getParticle(RandomSource rand, BlockState state) {

        if (!state.hasProperty(ReduxStates.PRISMATICNESS_DECREASED) || rand.nextFloat() < 0.2F) {
            return ReduxParticleTypes.FALLING_PRISMATIC_LEAVES.get();
        } else {
            int i = state.getValue(ReduxStates.PRISMATICNESS_DECREASED);
            // oh my god XD
            return i < this.particles.get().size() ? this.particles.get().get(i).get() :
            ReduxParticleTypes.FALLING_PRISMATIC_LEAVES.get();
        }
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ReduxStates.PRISMATICNESS_DECREASED);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        BlockState newState = this.getState(pState, pLevel, pCurrentPos);
        if (newState != pState)
        {
            pLevel.setBlock(pCurrentPos, newState, 3);
        }
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return this.getState(state, context.getLevel(), context.getClickedPos());
    }

    private BlockState getState(BlockState state, LevelAccessor level, BlockPos pos) {
        double scale = 0.1D;
        double x = pos.getX() * scale;
        double y = pos.getY() * scale;
        double z = pos.getZ() * scale;
        double noiseVal = FieldsprootLeafBlock.PERLIN.getValue(x, y, z);
        double clamped = Mth.clamp(noiseVal, -0.5D, 0.5D);
        int prism = Mth.lerpInt((float) clamped + 0.5F, 0, 2);
        return state.setValue(ReduxStates.PRISMATICNESS_DECREASED, prism);
    }


    /**
     * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
     */
    public void animateTick(BlockState block, Level world, BlockPos position, RandomSource rand) {
        super.animateTick(block, world, position, rand);
        if (rand.nextInt(15) == 0) {
            BlockPos blockpos = position.below();
            BlockState blockstate = world.getBlockState(blockpos);
            if (!blockstate.canOcclude() || !blockstate.isFaceSturdy(world, blockpos, Direction.UP)) {
                ParticleUtils.spawnParticleBelow(world, position, rand, getParticle(rand, blockstate));
            }
        }
    }
}
