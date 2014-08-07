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
        
        if(oH == 0) {
        	return;
        }

        // check the edges
        if ( ( pos.left >= wX ) && 
        		( pos.top >= wY ) && 
        		( oW + pos.left <= wX + wW ) && 
        		( oH + pos.top <= wY + wH ) ) {

            // fully visible
        	var counter = $(this).attr('counter');
            visible.push(counter);
            $(this).css('background-color', 'red');
            
            var minY = (pos.top - wY) / wH;
        	var maxY = minY + oH / wH;   
        	
        	var attention = calculateAttention(minY, maxY); 
        	
        	console.log( counter + "\t" + attention + "\t" + $(this).text());
            
        } else if ( ( ( ( pos.left <= wX ) && ( pos.left + oW > wX ) ) || 
        				( ( pos.left >= wX ) && ( pos.left <= wX + wW ) ) ) && 
        		( ( ( pos.top <= wY ) && ( pos.top + oH > wY ) ) || 
        				( ( pos.top >= wY ) && ( pos.top <= wY + wH ) ) ) ) {

        	// partially visible
        	var counter = $(this).attr('counter');
            visible.push(counter);
            $(this).css('background-color', 'red');
            
            var minY = (pos.top - wY) / wH;
        	var maxY = minY + oH / wH;   
        	
        	var attention = calculateAttention(minY, maxY);  
        	
        	console.log( counter + "\t" + attention + "\t" + $(this).text());
            
        }
    });
}

function calculateAttention(from, to) {
	if(from < 0) {
		from = 0;
	}
	if(from > 1) {
		from = 1;
	}
	if(to < 0) {
		to = 0;
	}
	if(to > 1) {
		to = 1;
	}
	return erf(to - 0.3) - erf(from - 0.3);
}

// http://en.wikipedia.org/wiki/Error_function#Approximation_with_elementary_functions
function erf(x) {
	var a = [0.078108, 0.000972, 0.230389, 0.278393];
	var denom = 0;
	for(var i in a) {
		denom += a[i];
		denom *= x;
	}
	denom += 1;
	var denom2 = denom * denom;
	var denom4 = denom2 * denom2;
	return 1 - 1.0 / denom4;
}