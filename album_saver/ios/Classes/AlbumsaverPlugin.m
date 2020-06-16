#import "AlbumsaverPlugin.h"

@implementation AlbumsaverPlugin {
    FlutterResult _result;
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"album_saver"
            binaryMessenger:[registrar messenger]];
  AlbumsaverPlugin* instance = [[AlbumsaverPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    _result = result;
    NSString *method = call.method;
    if ([@"saveImageToGallery" isEqualToString:method]) {
        NSData *imageData = ((FlutterStandardTypedData *)call.arguments).data;
        UIImage *image = [UIImage imageWithData:imageData];
        if (image == nil) {
            return;
        }
        UIImageWriteToSavedPhotosAlbum(image, self, @selector(didFinishSavingImage:error:contextInfo:), nil);
    } else if ([@"saveFileToGallery" isEqualToString:method]) {
        NSString *path = call.arguments;
        if ([self isImageFile:path]) {
            UIImage *image = [UIImage imageWithContentsOfFile:path];
            UIImageWriteToSavedPhotosAlbum(image, self, @selector(didFinishSavingImage:error:contextInfo:), nil);
        } else {
            if(UIVideoAtPathIsCompatibleWithSavedPhotosAlbum(path)) {
                UISaveVideoAtPathToSavedPhotosAlbum(path, self, @selector(didFinishSavingVideo:error:contextInfo:), nil);
            }
        }
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)didFinishSavingImage:(UIImage *)image error:(NSError *)error contextInfo:(id)contextInfo {
    if (_result != nil) {
        if (error) {
            _result(@false);
        } else {
            _result(@true);
        }
    }
}

- (void)didFinishSavingVideo:(NSString *)videoPath error:(NSError *)error contextInfo:(id)contextInfo {
    if (_result != nil) {
        if (error) {
            _result(@false);
         } else {
            _result(@true);
         }
    }
}


- (BOOL)isImageFile:(NSString *)filename {
    return [filename hasSuffix:@".jpg"]
    ||[filename hasSuffix:@".png"]
    ||[filename hasSuffix:@".JPEG"]
    ||[filename hasSuffix:@".JPG"]
    ||[filename hasSuffix:@".PNG"];
}


@end
