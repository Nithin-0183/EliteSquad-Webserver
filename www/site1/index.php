<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Simple Website</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f4f4;
        }
        header {
            background-color: #333;
            color: #fff;
            padding: 10px 0;
            text-align: center;
        }
        nav {
            display: flex;
            justify-content: center;
            background-color: #444;
        }
        nav a {
            color: #fff;
            padding: 14px 20px;
            text-decoration: none;
            text-align: center;
        }
        nav a:hover {
            background-color: #555;
        }
        .container {
            padding: 20px;
            max-width: 1200px;
            margin: auto;
            background-color: #fff;
        }
        .content {
            margin: 20px 0;
        }
        footer {
            background-color: #333;
            color: #fff;
            text-align: center;
            padding: 10px 0;
            position: fixed;
            width: 100%;
            bottom: 0;
        }
    </style>
</head>
<body>

<header>
    <h1>Welcome to My First Website</h1>
</header>

<nav>
    <a href="#">Home</a>
    <a href="#">About</a>
    <a href="#">Services</a>
    <a href="#">Contact</a>
</nav>

<div class="container">
    <div class="content">
        <h2>About Us</h2>
        <p>
            <?php
            // Simple PHP script to display a welcome message
            echo "Our company has been providing excellent services for over 10 years.";
            ?>
        </p>
        <p>
            We specialize in web development, digital marketing, and IT consulting. Our team of experts is dedicated to delivering high-quality solutions that help our clients succeed.
        </p>
    </div>

    <div class="content">
        <h2>Services</h2>
        <ul>
            <li>Web Development</li>
            <li>Digital Marketing</li>
            <li>IT Consulting</li>
            <li>SEO Optimization</li>
        </ul>
    </div>

    <div class="content">
        <h2>Contact Us</h2>
        <p>
            Have any questions? Reach out to us through our contact page or email us at <a href="mailto:info@example.com">info@example.com</a>.
        </p>
    </div>
</div>

<footer>
    &copy; <?php echo date("Y"); ?> My Simple Website. All rights reserved.
</footer>

</body>
</html>