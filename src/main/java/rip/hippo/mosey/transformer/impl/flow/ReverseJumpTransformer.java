/*
 * Mosey is a free and open source java bytecode obfuscator.
 *     Copyright (C) 2020  Hippo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package rip.hippo.mosey.transformer.impl.flow;

import rip.hippo.mosey.transformer.Transformer;
import rip.hippo.mosey.util.AsmUtil;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;


/**
 * @author Hippo
 * @version 1.0.0, 6/24/20
 * @since 1.0.0
 *
 * This transformer reverses jump opcodes which may or may not make it look weird on some decompilers.
 * It may make some if statements into ternary operations.
 *
 * <p>Before</p>
 * <code>
 *     ...
 *     IFNE l0
 *     ...
 *     l0
 * </code>
 *
 * <p>After</p>
 * <code>
 *     ...
 *     IFEQ l0
 *     GOTO l1 (previously l0)
 *     l0
 *     ...
 *     l1
 * </code>
 */
public final class ReverseJumpTransformer implements Transformer {

    @Override
    public void transform(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : method.instructions.toArray()) {
                int opcode = abstractInsnNode.getOpcode();
                if (opcode >= IFEQ && opcode <= IF_ACMPNE) {
                    JumpInsnNode jumpInsnNode = (JumpInsnNode) abstractInsnNode;
                    LabelNode offset = new LabelNode();
                    InsnList insnList = new InsnList();
                    insnList.add(new JumpInsnNode(GOTO, jumpInsnNode.label));
                    insnList.add(offset);
                    jumpInsnNode.setOpcode(AsmUtil.reverseJump(opcode));
                    jumpInsnNode.label = offset;
                    method.instructions.insert(jumpInsnNode, insnList);
                }
            }
        }
    }
}