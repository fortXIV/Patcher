package club.sk1er.patcher.tweaker.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * Used to string across multiple transformers that all do the same thing
 * without having to use duplicated code if it contains a {@link PatcherTransformer} method,
 * as those cannot be made a static method.
 */
public interface CommonTransformer extends PatcherTransformer {

    default void changeChatComponentHeight(MethodNode methodNode) {
        Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKESTATIC) {
                String methodInsnName = mapMethodNameFromNode(node);

                if (methodInsnName.equals("floor_float") || methodInsnName.equals("func_76141_d")) {
                    for (int i = 0; i < 4; ++i) {
                        node = node.getPrevious();
                    }

                    methodNode.instructions.insertBefore(node, minus12());
                    break;
                }
            }
        }
    }

    default InsnList minus12() {
        InsnList list = new InsnList();
        LabelNode afterSub = new LabelNode();
        list.add(getPatcherSetting("chatPosition", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, afterSub));
        list.add(new IincInsnNode(7, -12));
        list.add(afterSub);
        return list;
    }

    default InsnList modifyNametagRenderState(boolean voidReturnType) {
        InsnList list = new InsnList();
        list.add(getPatcherSetting("betterHideGui", "Z"));
        LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/Minecraft", isDevelopment() ? "getMinecraft" : "func_71410_x", "()Lnet/minecraft/client/Minecraft;", false));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", isDevelopment() ? "gameSettings" : "field_71474_y", "Lnet/minecraft/client/settings/GameSettings;"));
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/settings/GameSettings", isDevelopment() ? "hideGUI" : "field_74319_N", "Z"));
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        if (!voidReturnType) {
            list.add(new InsnNode(Opcodes.ICONST_0));
        }
        list.add(new InsnNode(voidReturnType ? Opcodes.RETURN : Opcodes.IRETURN));
        list.add(ifeq);
        return list;
    }
}
