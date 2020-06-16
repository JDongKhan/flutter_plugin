import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class Albumsaver {
  static const MethodChannel _channel = const MethodChannel('album_saver');

  /// save image to Gallery
  /// imageBytes can't null
  static Future<bool> saveImage(Uint8List imageBytes) async {
    assert(imageBytes != null);
    final bool result =
        await _channel.invokeMethod('saveImageToGallery', imageBytes);
    return result;
  }

  /// Save the PNG，JPG，JPEG image or video located at [file] to the local device media gallery.
  static Future<bool> saveFile(String file) async {
    assert(file != null);
    final bool result = await _channel.invokeMethod('saveFileToGallery', file);
    return result;
  }
}
