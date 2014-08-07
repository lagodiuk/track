$(document).ready(function() {
    $('button').click(markVisibleText);
});

function markVisibleText() {
	var visible = [];

    // check for visible spans with class 'track'   
    $('.track').each(function() {
    	
        var pos = $(this).offset(),
            oH = $(this).outerHeight(),
            oW = $(this).outerWidth(),
            wX = $(window).scrollLeft(),
            wY = $(window).scrollTop(),
            wH = $(window).height(),
            wW = $(window).width();

        $(this).css('background-color', '');

        // check the edges
        if ( ( pos.left >= wX ) && 
        		( pos.top >= wY ) && 
        		( oW + pos.left <= wX + wW ) && 
        		( oH + pos.top <= wY + wH ) ) {
        	
            // fully visible
            visible.push($(this).attr('counter'));
            $(this).css('background-color', 'red');
            
        } else if ( ( ( ( pos.left <= wX ) && ( pos.left + oW > wX ) ) || 
        				( ( pos.left >= wX ) && ( pos.left <= wX + wW ) ) ) && 
        		( ( ( pos.top <= wY ) && ( pos.top + oH > wY ) ) || 
        				( ( pos.top >= wY ) && ( pos.top <= wY + wH ) ) ) ) {

        	// partially visible
            visible.push($(this).attr('counter'));
            $(this).css('background-color', 'red');
            
        }
    });
}