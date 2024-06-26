package club.sk1er.patcher.asm.external.mods.pingtag;

import club.sk1er.patcher.tweaker.transform.CommonTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public class TagRendererTransformer implements CommonTransformer {

    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"me.powns.pingtag.rendering.TagRenderer"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            final String methodName = methodNode.name;
            if (methodName.equals("renderTag")) {
                final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    final AbstractInsnNode node = iterator.next();
                    if (node.getOpcode() == Opcodes.GETFIELD) {
                        final String fieldName = mapFieldNameFromNode(node);
                        if (fieldName.equals("playerViewX") || fieldName.equals("field_78732_j")) {
                            methodNode.instructions.insert(node, timesByModifier());
                            break;
                        }
                    }
                }
            }
        }
    }

    private InsnList timesByModifier() {
        InsnList list = new InsnList();
        list.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "club/sk1er/patcher/asm/external/mods/optifine/RenderTransformer",
                "checkPerspective",
                "()F",
                false));
        list.add(new InsnNode(Opcodes.FMUL));
        return list;
    }
}
