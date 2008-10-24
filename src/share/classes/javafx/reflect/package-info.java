/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/** Provides reflective access to JavaFX values and types.
 *
 * This packages defines a Java API (rather than a JavaFX API), 
 * so it can be used from both Java and JavaFX code.
 * A future JavaFX API may be layered on top of this.
 *
 * <h2>Values</h2>
 * The various reflection operations do not directly use
 * Java values.  Instead,
 * an {@link javafx.reflect.FXObjectValue} is a <q>handle</q> or
 * proxy for an <code>Object</code>.  This extra layer of indirection
 * isn't needed in many cases, bur it is useful for remote invocation,
 * remote control, or in general access to data in a different VM,
 *
 * <h2>Context</h2>
 * The objects in this package are directly or indirectly created
 * from a {@link javafx.reflect.FXContext FXContext}.
 * In the default case there
 * is a single {@code FXContext} instance that makes use of
 * Java reflection.  However, using a {@code FXContext} again
 * allows various kinds of indirection.
 *
 * <h2>Object creation</h2>
 * To do the equivalent of the JavaFX code:
 * <blockquote><pre>
 * var x = ...;
 * var z = Foo { a: 10; b: bind x.y };
 * </pre></blockquote>
 * you can do:
 * <blockquote><pre>
 * FXContext rcontext = ...;
 * FXClassType cls = rcontext.findClass(...);
 * FXObjectValue x = ...;
 * FXObjectValue z = cls.allocation();
 * z.initVar("a", ???);
 * z.bindVar("b", ???);
 * z = obj.initialize();
 * </pre></blockquote>
 *
 * <h2>Sequence operations</h2>
 * <p>
 * Use {@link javafx.reflect.FXSequenceBuilder} to create a new sequence.
 * <p>
 * To get the number of items in a sequence,
 * use {@link javafx.reflect.FXValue#getItemCount ValueRef.getItemCount}.
 * To index into a sequence,
 * use {@link javafx.reflect.FXValue#getItem ValueRef.getItem}.
 *
<h2>Design notes and issues</h2>
Some design principles, influenced by the "Mirrored reflection"
APIs (<a href="http://bracha.org/mirrors.pdf">Bracha and Ungar: <cite>Mirrors: Design Principles for Meta-level
Facilities of Object-Oritented Programming Languages</cite>, OOPSLA 2004</a>),
and <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdi/">JDI</a> :
<ul>
<li>No explicit constructors in user code.
<li>Keep everything abstract, and allow indirection.
For example, we might be working on objects in the current VM,
or a remote VM.  We might not have objects at all - while
not a priority, it would be nice to be able to use the same API for
(say) reading from .class files.
<li>Hence all classes (except for factory classes) are
interfaces or abstract classes.
<li> But try not to add too many levels of indirection
or "service lookup"!
</ul>
<h2>To do</h2>
<ul>
<li>Error handling - if (for example) there is
no method with a given name, do we return null or throw an exception?  Which exception?
<li>How to handle bound functions?
<li>???
</ul>
*/

package javafx.reflect;
