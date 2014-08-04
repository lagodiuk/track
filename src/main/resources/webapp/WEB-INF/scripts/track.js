$(document).ready(function(){

	$('button').click(function(){
	    var visible = [];
	    
	   // check for visible spans with class 'track'   
	    $('.track').each(function(){
	        var pos = $(this).offset(),
	            wX = $(window).scrollLeft(), wY = $(window).scrollTop(),
	            wH = $(window).height(), wW = $(window).width(),
	            oH = $(this).outerHeight(), oW = $(this).outerWidth();
	        
	        $(this).css('background-color', '');
	        
	        // check the edges
	        if (pos.left >= wX && pos.top >= wY && 
	            oW + pos.left <= wX + wW && oH + pos.top <= wY + wH ) {
	            //alert('Span #' + $(this).attr('counter') + ' is fully visible');
	            visible.push( $(this).attr('counter') );
	            $(this).css('background-color', 'red');
	        } else if (((pos.left <= wX && pos.left + oW > wX) ||
	              (pos.left >= wX && pos.left <= wX + wW)) &&
	            ((pos.top <= wY && pos.top + oH > wY)   ||
	             (pos.top  >= wY && pos.top  <= wY + wH))) {
	            //alert('Span #' + $(this).attr('counter') + ' is partially visible');
	            visible.push( $(this).attr('counter') );
	            $(this).css('background-color', 'red');
	        } else {
	            //alert('Span #' + $(this).attr('counter') + ' is not visible');
	        }
	    });        
	    
	    alert('Visible: ' + JSON.stringify(visible, null, 0));
	});
    
    alert('done!')
});