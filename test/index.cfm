<html>
<body>
  <ul>
    <li><a href="classic_basic.cfm">Classic engine, basic</a></li>
    <li><a href="modern_basic.cfm">Modern engine, basic</a></li>
    <li><a href="modern_section.cfm">Modern engine, with sections</a></li>
    <!--- <li><a href="classic_mixed_orientation.cfm">Classic engine, mixed orientation</a></li>
    <li><a href="modern_mixed_orientation.cfm">Modern engine, mixed orientation</a></li> --->
  </ul>

  <script>
    var body = document.querySelector('body');

    document.querySelectorAll('li').forEach(li => {
      var div = document.createElement('div');
      div.style.display = 'flex';

      var iframe = document.createElement('iframe');
      iframe.src = li.querySelector('a').href;
      iframe.style.width = '50%';
      iframe.style.height = '1000px';
      div.appendChild(iframe);

      var img = document.createElement('img');
      var imgName = li.querySelector('a').href
        .replace(/^.*\//, '')
        .replace(/cfm/, 'png');
      img.src = `expected_output/${imgName}`;
      img.style.display = 'inline-block';
      img.style.width = '50%';
      div.appendChild(img);

      body.append(div);
    });
  </script>
</body>
</html>
