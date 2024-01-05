package dev.liquid.osamaclient.util

import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos

fun EntityLivingBase.getActualPosition() = BlockPos(posX, posY, posZ)