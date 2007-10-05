package javafx.ui.canvas;
import javafx.ui.*;

public abstract class AbstractVisualNode {
    /** The <code>stroke</code> property paints along the outline of the given 
     * graphical element.
     */
    public attribute stroke: Paint;

    /**
     * The <code>fill</code> property paints the interior of the given 
     graphical element. The area to be painted consists of any areas inside 
     * the outline of the node's shape. 
     */
    public attribute fill: Paint;

    /**
     * <code>strokeLineCap</code> specifies the shape to be used at the end 
     * of open subpaths when they are stroked. Defaults to <code>SQUARE</code>.
     */
    public attribute strokeLineCap: StrokeLineCap;
    /**
     * <code>strokeLineJoin</code> specifies the shape to be used at the 
     * corners of paths or basic shapes when they are stroked. Defaults to
     * <code>MITER</code>.
     */
    public attribute strokeLineJoin: StrokeLineJoin;
    /**
     * The width of the stroke on the current object. A zero value causes 
     * no stroke to be painted. A negative value is an error. Defaults to 1.0.
     */
    public attribute strokeWidth: Number;
    /**
     * <p>
     * When two line segments meet at a sharp angle and miter joins have
     * been specified for 'stroke-linejoin', it is possible for the miter to
     * extend far beyond the thickness of the line stroking the path. The 
     * 'strokeMiterLimit' imposes a limit on the ratio of the miter length to
     * the 'strokeWidth'. When the limit is exceeded, the join is converted
     * from a miter to a bevel.
     * </p>
     * <p>
     * <b>strokeMiterLimit</b></br>
     * The limit on the ratio of the miter length to the 'strokeWidth'. The
     * value of <miterlimit> must be a number greater than or equal to 1.
     * Any other value is an error. Defaults to 10.0.
     * </p>
     * <p>
     * The ratio of miter length (distance between the outer tip and the inner
     * corner of the miter) to 'strokeWidth' is directly related to the angle
     * (theta) between the segments in user space by the formula:
     * </p>
     * <p><code>
     * &nbsp;&nbsp;&nbsp;&nbsp;miterLength / strokeWidth = 1 / sin(theta/2)
     * </pre></code>
     * </p>
     * <p>
     * For example, a miter limit of 1.414 converts miters to bevels for 
     * theta less than 90 degrees, a limit of 4.0 converts them for theta less
     * than approximately 29 degrees, and a limit of 10.0 converts them for 
     * theta less than approximately 11.5 degrees.
     * </p>
     */
    public attribute strokeMiterLimit: Number;
    /**
     * <code>strokeDashArray</code> controls the pattern of dashes and gaps 
     * used to stroke paths. This attribute contains a list of lengths that 
     * specify the lengths of alternating dashes and gaps. If an odd number 
     * of values is provided, then the list of values is repeated to yield an 
     * even number of values. Thus, strokeDashArray: [5,3,2] is equivalent to
     * strokeDashArray: [5,3,2,5,3,2]. Defaults to [].
     */
    public attribute strokeDashArray: Number[];
    /** 
     * <code>strokeDashOffset</code> specifies the distance into the dash 
     * pattern to start the dash. Defaults to 0.0.
     */
    public attribute strokeDashOffset: Number;

}
