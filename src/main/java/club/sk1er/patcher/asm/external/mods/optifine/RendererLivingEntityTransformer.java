package club.sk1er.patcher.asm.external.mods.optifine;

import club.sk1er.patcher.tweaker.transform.CommonTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class RendererLivingEntityTransformer implements CommonTransformer {

    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.renderer.entity.RendererLivingEntity"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        boolean transformedDoRender = false;
        for (MethodNode methodNode : classNode.methods) {
            final String methodName = mapMethodName(classNode, methodNode);
            if (!transformedDoRender && (methodName.equals("doRender") || methodName.equals("func_76986_a"))) {
                final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                int fIndex = -1;
                int f1Index = -1;
                int f2Index = -1;

                for (final LocalVariableNode var : methodNode.localVariables) {
                    switch (var.name) {
                        case "var10":
                        case "f":
                            fIndex = var.index;
                            break;

                        case "var11":
                        case "f1":
                            f1Index = var.index;
                            break;

                        case "var12":
                        case "f2":
                            f2Index = var.index;
                            break;
                    }
                }

                /*
                Find if (shouldSit && entity.ridingEntity instanceof EntityLivingBase)
                    go forward until we find the label we jump to if that statement is false, retract 1 insn, then read f2 = f1 -f
                 */

                while (iterator.hasNext()) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode && next.getOpcode() == Opcodes.INVOKESTATIC) {
                        final String methodInsnName = mapMethodNameFromNode(next);
                        if (methodInsnName.equals("disableCull") || methodInsnName.equals("func_179129_p")) {
                            methodNode.instructions.insertBefore(next, startCulling());
                            methodNode.instructions.remove(next);
                        } else if (methodInsnName.equals("enableCull") || methodInsnName.equals("func_179089_o")) {
                            methodNode.instructions.insertBefore(next, new MethodInsnNode(
                                Opcodes.INVOKESTATIC, getHookClass("RendererLivingEntityHook"), "backFaceCullingEnd", "()V", false
                            ));
                            methodNode.instructions.remove(next);
                        }
                    } else if (next instanceof TypeInsnNode) {
                        //#if MC==10809
                        if (next.getOpcode() == Opcodes.INSTANCEOF && ((TypeInsnNode) next).desc.equals("net/minecraft/entity/EntityLivingBase")) {
                            LabelNode node = null; //Find label
                            while ((next = next.getNext()) != null) {
                                if (next instanceof JumpInsnNode && next.getOpcode() == Opcodes.IFEQ) {
                                    node = ((JumpInsnNode) next).label;
                                    break;
                                }
                            }

                            if (next == null) {
                                transformedDoRender = true;
                                break;
                            }

                            LabelNode labelNode = new LabelNode(); //Override final if statement to jump to our new end of block
                            while ((next = next.getNext()) != null) {
                                if (next instanceof JumpInsnNode && ((JumpInsnNode) next).label.equals(node)
                                    && next.getOpcode() == Opcodes.IFLE) {
                                    ((JumpInsnNode) next).label = labelNode;
                                    break;
                                }
                            }

                            if (next == null) {
                                transformedDoRender = true;
                                break;
                            }

                            while ((next = next.getNext()) != null) {
                                if (next == node) {
                                    InsnList insnList = new InsnList();
                                    insnList.add(labelNode);
                                    insnList.add(new VarInsnNode(Opcodes.FLOAD, f1Index));
                                    insnList.add(new VarInsnNode(Opcodes.FLOAD, fIndex));
                                    insnList.add(new InsnNode(Opcodes.FSUB));
                                    insnList.add(new VarInsnNode(Opcodes.FSTORE, f2Index));
                                    methodNode.instructions.insertBefore(next, insnList);
                                    transformedDoRender = true;
                                    break;
                                }
                            }
                        }
                        //#endif
                    }
                }
            } else if (methodName.equals("rotateCorpse") || methodName.equals("func_77043_a")) {
                final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    final AbstractInsnNode next = iterator.next();
                    if (next instanceof LdcInsnNode && ((LdcInsnNode) next).cst.equals(0.1F)) {
                        methodNode.instructions.remove(next.getPrevious());
                        methodNode.instructions.insertBefore(next,
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                getHookClass("RendererLivingEntityHook"), "getVisibleHeight", "(Lnet/minecraft/entity/Entity;)F",
                                false
                            ));
                    }
                }
            }
        }
    }

    private InsnList startCulling() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, getHookClass("RendererLivingEntityHook"),
            "backFaceCullingStart", "(Lnet/minecraft/entity/Entity;)V", false));
        return list;
    }
}
