/*
 * @subtest
 */

public class XGroup extends XParent {

    public var content: XNode[];

    override var children = bind content with inverse;
    
}