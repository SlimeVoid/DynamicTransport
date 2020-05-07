package net.slimevoid.dynamictransport.blocks;


import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.slimevoid.dynamictransport.tileentity.ElevatorControllerTileEntitiy;
import net.slimevoid.dynamictransport.tileentity.TransportPartTileEntity;

public class MarkerBlock extends BaseTransportPartBlock {
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> container) {
        super.fillStateContainer(container);
        container.add(RedstoneDiodeBlock.POWERED);
    }
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isRemote()) {
            boolean flag = worldIn.isBlockPowered(pos);
            if (state.get(RedstoneDiodeBlock.POWERED) != flag) {
                worldIn.setBlockState(pos, state.with(RedstoneDiodeBlock.POWERED, flag));
                if (flag) {
                    TileEntity e = worldIn.getTileEntity(pos);
                    if (e instanceof TransportPartTileEntity) {
                        BlockPos controllerPos = ((TransportPartTileEntity) e).getController();
                        if (controllerPos != null) {
                            TileEntity c = worldIn.getTileEntity(controllerPos);
                            if (c instanceof ElevatorControllerTileEntitiy) {
                                ((ElevatorControllerTileEntitiy) c).callElevator(pos.getY() + -2, null);
                                //send banner
                                worldIn.getServer().getPlayerList().sendToAllNearExcept(null, pos.getX(),pos.getY(),pos.getZ(),4,worldIn.getDimension().getType(),
                                        new SChatPacket( new TranslationTextComponent("elevator.called"), ChatType.GAME_INFO));
                            }
                        }
                    }
                }
            }
        }
    }
}
