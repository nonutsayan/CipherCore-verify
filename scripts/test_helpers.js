// Test script for canvas functions
function wrapText(ctx, text, x, y, maxWidth, lineHeight, maxLines) {
  var words = text.split(' ');
  var line = '';
  var lines = [];
  for (var n = 0; n < words.length; n++) {
    var testLine = line + (line ? ' ' : '') + words[n];
    var metrics = ctx.measureText(testLine);
    if (metrics.width > maxWidth && n > 0) {
      lines.push(line);
      line = words[n];
    } else {
      line = testLine;
    }
  }
  lines.push(line);
  if (maxLines && lines.length > maxLines) {
    lines = lines.slice(0, maxLines);
    if (lines[maxLines - 1].length > 3) {
      var last = lines[maxLines - 1];
      while (ctx.measureText(last + '...').width > maxWidth && last.length > 0) {
        last = last.slice(0, -1);
      }
      lines[maxLines - 1] = last + '...';
    }
  }
  for (var k = 0; k < lines.length; k++) {
    ctx.fillText(lines[k], x, y + (k * lineHeight));
  }
  return y + (lines.length * lineHeight);
}

function roundRect(ctx, x, y, width, height, radius, fill, stroke) {
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.lineTo(x + width - radius, y);
  ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
  ctx.lineTo(x + width, y + height - radius);
  ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
  ctx.lineTo(x + radius, y + height);
  ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
  ctx.lineTo(x, y + radius);
  ctx.quadraticCurveTo(x, y, x + radius, y);
  ctx.closePath();
  if (fill) ctx.fill();
  if (stroke) ctx.stroke();
}

console.log('Helpers defined without syntax error.');
